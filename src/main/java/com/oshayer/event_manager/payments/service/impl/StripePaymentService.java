package com.oshayer.event_manager.payments.service.impl;

import com.oshayer.event_manager.events.entity.EventSeatEntity;
import com.oshayer.event_manager.payments.config.StripeProperties;
import com.oshayer.event_manager.payments.dto.CreatePaymentIntentRequest;
import com.oshayer.event_manager.payments.dto.CreatePaymentIntentResponse;
import com.oshayer.event_manager.payments.entity.PaymentEntity;
import com.oshayer.event_manager.payments.entity.PaymentStatus;
import com.oshayer.event_manager.payments.repository.PaymentRepository;
import com.oshayer.event_manager.payments.service.PaymentService;
import com.oshayer.event_manager.ticketing.dto.HoldConvertRequest;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity.HoldStatus;
import com.oshayer.event_manager.ticketing.repository.ReservationHoldRepository;
import com.oshayer.event_manager.ticketing.service.ReservationHoldService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StripePaymentService implements PaymentService {

    private final ReservationHoldRepository holdRepository;
    private final ReservationHoldService reservationHoldService;
    private final PaymentRepository paymentRepository;
    private final StripeProperties stripeProperties;

    @Override
    public CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) throws StripeException {
        if (!StringUtils.hasText(stripeProperties.getSecretKey())) {
            throw new IllegalStateException("Stripe secret key is not configured");
        }
        ReservationHoldEntity hold = holdRepository.findById(request.getHoldId())
                .orElseThrow(() -> new EntityNotFoundException("Hold not found"));
        validateHold(hold);

        long amountCents = calculateHoldAmountCents(hold);
        String currency = resolveCurrency(request.getCurrency());

        PaymentEntity payment = PaymentEntity.builder()
                .hold(hold)
                .event(hold.getEvent())
                .customerEmail(request.getCustomerEmail())
                .amountCents(amountCents)
                .currency(currency)
                .description(request.getDescription())
                .status(PaymentStatus.REQUIRES_PAYMENT_METHOD)
                .build();
        payment = paymentRepository.save(payment);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("paymentId", payment.getId().toString());
        metadata.put("holdId", hold.getId().toString());
        metadata.put("eventId", hold.getEvent().getId().toString());

        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency(currency)
                .putAllMetadata(metadata)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                );

        if (StringUtils.hasText(request.getCustomerEmail())) {
            paramsBuilder.setReceiptEmail(request.getCustomerEmail());
        }
        if (StringUtils.hasText(request.getDescription())) {
            paramsBuilder.setDescription(request.getDescription());
        }

        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(payment.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(paramsBuilder.build(), requestOptions);
        payment.setStripePaymentIntentId(intent.getId());
        payment.setStatus(mapStripeStatus(intent.getStatus()));
        paymentRepository.save(payment);

        return CreatePaymentIntentResponse.builder()
                .paymentId(payment.getId())
                .paymentIntentId(intent.getId())
                .clientSecret(intent.getClientSecret())
                .amountCents(amountCents)
                .currency(currency)
                .status(payment.getStatus())
                .build();
    }

    @Override
    public void handleWebhook(String payload, String signatureHeader) throws SignatureVerificationException, StripeException {
        if (!StringUtils.hasText(stripeProperties.getWebhookSecret())) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }
        Event event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.getWebhookSecret());
        if (!event.getType().startsWith("payment_intent.")) {
            log.debug("Ignoring unsupported Stripe event type {}", event.getType());
            return;
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        PaymentIntent intent = (PaymentIntent) deserializer.getObject()
                .orElseThrow(() -> new IllegalStateException("Stripe event payload missing object"));
        updatePaymentFromIntent(intent);
    }

    private void updatePaymentFromIntent(PaymentIntent intent) {
        paymentRepository.findByStripePaymentIntentId(intent.getId())
                .ifPresentOrElse(payment -> {
                    PaymentStatus newStatus = mapStripeStatus(intent.getStatus());
                    payment.setStatus(newStatus);
                    payment.setLastError(intent.getLastPaymentError() != null
                            ? intent.getLastPaymentError().getMessage()
                            : null);
                    paymentRepository.save(payment);

                    if (newStatus == PaymentStatus.SUCCEEDED) {
                        finalizeHold(payment);
                    } else if (newStatus == PaymentStatus.CANCELED || newStatus == PaymentStatus.FAILED) {
                        releaseHoldOnFailure(payment);
                    }
                }, () -> log.warn("No payment record found for Stripe intent {}", intent.getId()));
    }

    private void finalizeHold(PaymentEntity payment) {
        try {
            reservationHoldService.convert(new HoldConvertRequest(
                    payment.getHold().getId(),
                    payment.getId()
            ));
        } catch (Exception ex) {
            log.error("Failed to convert hold {} for payment {}", payment.getHold().getId(), payment.getId(), ex);
            payment.setLastError("Hold conversion failed: " + ex.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    private void releaseHoldOnFailure(PaymentEntity payment) {
        ReservationHoldEntity hold = payment.getHold();
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            return;
        }
        hold.setStatus(HoldStatus.RELEASED);
        if (hold.getHeldSeats() != null) {
            hold.getHeldSeats().forEach(seat -> seat.setStatus(EventSeatEntity.EventSeatStatus.AVAILABLE));
        }
        holdRepository.save(hold);
    }

    private void validateHold(ReservationHoldEntity hold) {
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new IllegalStateException("Hold is not active");
        }
        if (hold.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Hold has expired");
        }
        if (hold.getHeldSeats() == null || hold.getHeldSeats().isEmpty()) {
            throw new IllegalStateException("Hold does not contain any seats");
        }
    }

    private long calculateHoldAmountCents(ReservationHoldEntity hold) {
        BigDecimal total = hold.getHeldSeats().stream()
                .map(seat -> {
                    if (seat.getPrice() == null) {
                        throw new IllegalStateException("Seat price missing for seat " + seat.getId());
                    }
                    return seat.getPrice();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.movePointRight(2).longValueExact();
    }

    private PaymentStatus mapStripeStatus(String stripeStatus) {
        if (stripeStatus == null) {
            return PaymentStatus.REQUIRES_PAYMENT_METHOD;
        }
        return switch (stripeStatus) {
            case "requires_payment_method" -> PaymentStatus.REQUIRES_PAYMENT_METHOD;
            case "requires_confirmation" -> PaymentStatus.REQUIRES_CONFIRMATION;
            case "requires_action", "requires_capture" -> PaymentStatus.REQUIRES_ACTION;
            case "processing" -> PaymentStatus.PROCESSING;
            case "succeeded" -> PaymentStatus.SUCCEEDED;
            case "canceled" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.FAILED;
        };
    }

    private String resolveCurrency(String requestedCurrency) {
        if (StringUtils.hasText(requestedCurrency)) {
            return requestedCurrency.toLowerCase();
        }
        if (StringUtils.hasText(stripeProperties.getCurrency())) {
            return stripeProperties.getCurrency().toLowerCase();
        }
        return "usd";
    }
}
