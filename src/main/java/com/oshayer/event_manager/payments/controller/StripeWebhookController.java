package com.oshayer.event_manager.payments.controller;

import com.oshayer.event_manager.payments.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripeWebhookController {

    private static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(name = STRIPE_SIGNATURE_HEADER, required = false) String signature,
            @RequestBody String payload) {
        if (signature == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Stripe-Signature header");
        }
        try {
            paymentService.handleWebhook(payload, signature);
            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException ex) {
            log.warn("Invalid Stripe signature", ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature", ex);
        } catch (StripeException ex) {
            log.error("Error handling Stripe webhook", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe webhook processing error", ex);
        } catch (IllegalStateException ex) {
            log.error("Stripe webhook rejected", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }
}
