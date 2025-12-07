package com.oshayer.event_manager.ticketing.service.impl;

import com.oshayer.event_manager.discounts.dto.DiscountCalculationRequest;
import com.oshayer.event_manager.discounts.dto.DiscountCalculationResult;
import com.oshayer.event_manager.discounts.dto.DiscountLineItem;
import com.oshayer.event_manager.discounts.dto.DiscountValidationResponseItem;
import com.oshayer.event_manager.discounts.service.DiscountService;
import com.oshayer.event_manager.events.entity.EventEntity;
import com.oshayer.event_manager.events.entity.EventSeatEntity;
import com.oshayer.event_manager.events.entity.EventSeatEntity.EventSeatStatus;
import com.oshayer.event_manager.events.entity.EventTicketTier;
import com.oshayer.event_manager.events.repository.EventRepository;
import com.oshayer.event_manager.events.repository.EventSeatRepository;
import com.oshayer.event_manager.events.repository.EventTicketTierRepository;
import com.oshayer.event_manager.seat.entity.SeatEntity;
import com.oshayer.event_manager.seat.entity.SeatLayout;
import com.oshayer.event_manager.seat.repository.SeatLayoutRepository;
import com.oshayer.event_manager.seat.repository.SeatRepository;
import com.oshayer.event_manager.venues.entity.EventVenue;
import com.oshayer.event_manager.venues.repository.EventVenueRepository;
import com.oshayer.event_manager.ticketing.dto.HoldConvertRequest;
import com.oshayer.event_manager.ticketing.dto.HoldCreateRequest;
import com.oshayer.event_manager.ticketing.dto.HoldReleaseRequest;
import com.oshayer.event_manager.ticketing.dto.HoldResponse;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldDiscountEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity.HoldStatus;
import com.oshayer.event_manager.ticketing.repository.ReservationHoldRepository;
import com.oshayer.event_manager.ticketing.service.ReservationHoldService;
import com.oshayer.event_manager.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationHoldServiceImpl implements ReservationHoldService {

    private final ReservationHoldRepository holdRepo;
    private final EventSeatRepository eventSeatRepo;
    private final EventRepository eventRepo;
    private final EventTicketTierRepository eventTicketTierRepository;
    private final SeatRepository seatRepository;
    private final SeatLayoutRepository seatLayoutRepository;
    private final EventVenueRepository venueRepository;
    private final UserRepository userRepo;
    private final DiscountService discountService;

    @Override
    public HoldResponse create(HoldCreateRequest req) {
        if (req.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new IllegalArgumentException("expiresAt must be in the future");

        if (req.getBuyerId() == null && req.getDiscountCode() != null && !req.getDiscountCode().isBlank()) {
            throw new IllegalStateException("You must be signed in to redeem discount codes.");
        }

        var event = eventRepo.findById(req.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        List<UUID> seatIds = req.getSeatIds() != null ? req.getSeatIds().stream().filter(Objects::nonNull).toList() : List.of();
        List<HoldCreateRequest.TierSelection> tierSelections = req.getTierSelections() != null ? req.getTierSelections() : List.of();

        if (seatIds.isEmpty() && tierSelections.isEmpty()) {
            throw new IllegalArgumentException("Provide seatIds or tierSelections when creating a hold.");
        }
        if (!seatIds.isEmpty() && !tierSelections.isEmpty()) {
            throw new IllegalArgumentException("Use either seatIds or tierSelections, not both.");
        }

        List<EventSeatEntity> seatsToHold = !seatIds.isEmpty()
                ? reserveExplicitSeats(event, seatIds)
                : reserveGeneralAdmissionSeats(event, tierSelections);

        DiscountCalculationResult discountResult = discountService.calculateForHold(
                DiscountCalculationRequest.builder()
                        .eventId(event.getId())
                        .buyerId(req.getBuyerId())
                        .discountCode(req.getDiscountCode())
                        .includeAutomaticDiscounts(true)
                        .items(buildLineItems(seatsToHold))
                        .build());

        var holdBuilder = ReservationHoldEntity.builder()
                .event(event)
                .status(HoldStatus.ACTIVE)
                .heldSeats(seatsToHold)
                .itemsJson(buildItemsJson(seatsToHold))
                .expiresAt(req.getExpiresAt())
                .subtotalAmount(discountResult.getSubtotal())
                .discountAmount(discountResult.getDiscountTotal())
                .totalAmount(discountResult.getTotalDue());

        if (req.getBuyerId() != null) {
            var buyer = userRepo.findById(req.getBuyerId())
                    .orElseThrow(() -> new EntityNotFoundException("Buyer not found"));
            holdBuilder.buyer(buyer);
        }

        ReservationHoldEntity hold = holdBuilder.build();
        hold.setAppliedDiscounts(mapAppliedDiscounts(hold, discountResult));

        ReservationHoldEntity h = holdRepo.save(hold);
        return toResponse(h);
    }

    @Override
    public HoldResponse release(HoldReleaseRequest req) {
        ReservationHoldEntity h = holdRepo.findById(req.getHoldId())
                .orElseThrow(() -> new EntityNotFoundException("Hold not found"));
        if (h.getStatus() != HoldStatus.ACTIVE) return toResponse(h);

        // Make all held seats available again
        for (EventSeatEntity seat : h.getHeldSeats()) {
            seat.setStatus(EventSeatStatus.AVAILABLE);
        }

        h.setStatus(HoldStatus.RELEASED);
        return toResponse(h);
    }

    @Override
    public HoldResponse convert(HoldConvertRequest req) {
        ReservationHoldEntity h = holdRepo.findById(req.getHoldId())
                .orElseThrow(() -> new EntityNotFoundException("Hold not found"));
        if (h.getStatus() != HoldStatus.ACTIVE)
            throw new IllegalStateException("Only ACTIVE holds can be converted");
        if (h.getExpiresAt().isBefore(OffsetDateTime.now())) {
            // When a hold expires, release the seats
            for (EventSeatEntity seat : h.getHeldSeats()) {
                seat.setStatus(EventSeatStatus.AVAILABLE);
            }
            h.setStatus(HoldStatus.EXPIRED);
            throw new IllegalStateException("Hold has expired");
        }

        h.setStatus(HoldStatus.CONVERTED);
        h.setFinalizedPaymentId(req.getPaymentId());
        discountService.recordRedemptionForHold(h);
        // Note: The seats remain RESERVED. They will be marked as SOLD when the actual ticket is issued.
        return toResponse(h);
    }

    @Override
    @Transactional(readOnly = true)
    public HoldResponse get(UUID holdId) {
        return holdRepo.findById(holdId).map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Hold not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoldResponse> listActive(UUID eventId) {
        return holdRepo.findActiveNotExpired(eventId, OffsetDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    // -------- helper --------

    private List<EventSeatEntity> reserveExplicitSeats(EventEntity event, List<UUID> seatIds) {
        List<EventSeatEntity> seatsToHold = new ArrayList<>();
        for (UUID seatId : seatIds) {
            EventSeatEntity seat = eventSeatRepo.findByEventIdAndSeatId(event.getId(), seatId)
                    .orElseThrow(() -> new EntityNotFoundException("Seat with ID " + seatId + " not found for this event"));

            if (seat.getStatus() != EventSeatStatus.AVAILABLE) {
                throw new IllegalStateException("Seat " + seat.getSeat().getLabel() + " is not available.");
            }
            seat.setStatus(EventSeatStatus.RESERVED);
            seatsToHold.add(seat);
        }
        return seatsToHold;
    }

    private List<EventSeatEntity> reserveGeneralAdmissionSeats(EventEntity event, List<HoldCreateRequest.TierSelection> tierSelections) {
        if (tierSelections.isEmpty()) {
            throw new IllegalArgumentException("tierSelections must not be empty when seatIds are not provided.");
        }

        List<EventTicketTier> tiers = eventTicketTierRepository.findByEventId(event.getId());
        if (tiers.isEmpty()) {
            throw new IllegalStateException("Event has no ticket tiers configured.");
        }

        Map<String, EventTicketTier> tiersByCode = tiers.stream()
                .collect(Collectors.toMap(EventTicketTier::getTierCode, t -> t, (a, b) -> a, LinkedHashMap::new));

        SeatLayout layout = null;
        List<EventSeatEntity> seatsToHold = new ArrayList<>();

        for (HoldCreateRequest.TierSelection selection : tierSelections) {
            String tierCode = selection.getTierCode();
            EventTicketTier tier = tiersByCode.get(tierCode);
            if (tier == null) {
                throw new IllegalArgumentException("Tier code " + tierCode + " is not valid for this event.");
            }
            int quantity = selection.getQuantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity for tier " + tierCode + " must be positive.");
            }

            long sold = tier.getSoldQuantity() == null ? 0 : tier.getSoldQuantity();
            long reserved = eventSeatRepo.countByEventAndTierAndStatuses(
                    event.getId(), tierCode, List.of(EventSeatStatus.RESERVED, EventSeatStatus.BLOCKED));
            long available = tier.getTotalQuantity() - sold - reserved;
            if (available < quantity) {
                throw new IllegalStateException("Only " + Math.max(available, 0) + " tickets remain for tier " + tierCode + ".");
            }

            List<EventSeatEntity> reusable = eventSeatRepo.findAvailableSeats(
                    event.getId(), tierCode, EventSeatStatus.AVAILABLE, PageRequest.of(0, quantity));
            reusable.forEach(seat -> seat.setStatus(EventSeatStatus.RESERVED));
            if (!reusable.isEmpty()) {
                eventSeatRepo.saveAll(reusable);
            }
            seatsToHold.addAll(reusable);

            int remaining = quantity - reusable.size();
            if (remaining > 0) {
                if (layout == null) {
                    layout = ensureGeneralAdmissionSeatLayout(event, tiers);
                }
                long existing = eventSeatRepo.countByEvent_IdAndTierCode(event.getId(), tierCode);
                for (int i = 0; i < remaining; i++) {
                    long index = existing + i + 1;
                    String label = tierCode + "-GA-" + String.format("%04d", index);

                    SeatEntity seatEntity = SeatEntity.builder()
                            .seatLayout(layout)
                            .row(tierCode)
                            .number((int) index)
                            .label(label)
                            .type("GENERAL")
                            .build();
                    seatEntity = seatRepository.save(seatEntity);

                    EventSeatEntity eventSeat = EventSeatEntity.builder()
                            .event(event)
                            .seat(seatEntity)
                            .tierCode(tierCode)
                            .price(tier.getPrice())
                            .status(EventSeatStatus.RESERVED)
                            .build();
                    eventSeat = eventSeatRepo.save(eventSeat);
                    seatsToHold.add(eventSeat);
                }
            }
        }

        return seatsToHold;
    }

    private SeatLayout ensureGeneralAdmissionSeatLayout(EventEntity event, List<EventTicketTier> tiers) {
        if (event.getSeatLayoutId() != null) {
            return seatLayoutRepository.findById(event.getSeatLayoutId())
                    .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + event.getSeatLayoutId()));
        }

        EventVenue venue = venueRepository.findById(event.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found: " + event.getVenueId()));

        int capacity = tiers.stream().mapToInt(EventTicketTier::getTotalQuantity).sum();
        String baseName = "Freestyle layout - " + event.getEventName();
        String layoutName = baseName;
        int suffix = 1;
        while (seatLayoutRepository.existsByVenue_IdAndLayoutName(venue.getId(), layoutName)) {
            layoutName = baseName + " (" + suffix++ + ")";
        }

        SeatLayout freestyleLayout = SeatLayout.builder()
                .venue(venue)
                .typeCode("220")
                .typeName("Freestyle")
                .layoutName(layoutName)
                .totalRows(tiers.size())
                .totalCols(capacity)
                .standingCapacity(capacity)
                .totalCapacity(capacity)
                .isActive(true)
                .build();

        freestyleLayout = seatLayoutRepository.save(freestyleLayout);
        event.setSeatLayoutId(freestyleLayout.getId());
        eventRepo.save(event);
        return freestyleLayout;
    }
    private HoldResponse toResponse(ReservationHoldEntity h) {
        List<HoldResponse.HeldSeatInfo> heldSeatInfo = h.getHeldSeats().stream()
                .map(es -> HoldResponse.HeldSeatInfo.builder()
                        .seatId(es.getSeat().getId())
                        .seatLabel(es.getSeat().getLabel())
                        .tierCode(es.getTierCode())
                        .build())
                .collect(Collectors.toList());

        return HoldResponse.builder()
                .id(h.getId())
                .eventId(h.getEvent().getId())
                .buyerId(h.getBuyer() != null ? h.getBuyer().getId() : null)
                .status(h.getStatus().name())
                .heldSeats(heldSeatInfo)
                .expiresAt(h.getExpiresAt())
                .finalizedPaymentId(h.getFinalizedPaymentId())
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .subtotalAmount(h.getSubtotalAmount())
                .discountAmount(h.getDiscountAmount())
                .totalAmount(h.getTotalAmount())
                .appliedDiscounts(mapAppliedDiscounts(h))
                .build();
    }

    private String buildItemsJson(List<EventSeatEntity> seats) {
        return seats.stream()
                .map(es -> "{" +
                        "\"seatId\":\"" + es.getId() + "\"," +
                        "\"seatLabel\":\"" + escapeJson(es.getSeat().getLabel()) + "\"," +
                        "\"tierCode\":\"" + escapeJson(es.getTierCode()) + "\"," +
                        "\"price\":\"" + (es.getPrice() != null ? es.getPrice() : BigDecimal.ZERO) + "\"" +
                        "}")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private List<DiscountLineItem> buildLineItems(List<EventSeatEntity> seats) {
        return seats.stream().map(seat -> {
            if (seat.getPrice() == null) {
                throw new IllegalStateException("Seat " + seat.getId() + " is missing a price");
            }
            return DiscountLineItem.builder()
                    .seatId(seat.getId())
                    .tierCode(seat.getTierCode())
                    .quantity(1)
                    .unitPrice(seat.getPrice())
                    .build();
        }).collect(Collectors.toList());
    }

    private List<ReservationHoldDiscountEntity> mapAppliedDiscounts(ReservationHoldEntity hold,
                                                                    DiscountCalculationResult result) {
        if (result.getAppliedDiscountEntities() == null || result.getAppliedDiscountEntities().isEmpty()) {
            return new ArrayList<>();
        }

        List<ReservationHoldDiscountEntity> applied = new ArrayList<>();
        List<DiscountValidationResponseItem> summaries = result.getAppliedDiscounts();

        for (int i = 0; i < result.getAppliedDiscountEntities().size(); i++) {
            var discount = result.getAppliedDiscountEntities().get(i);
            var summary = summaries.get(i);
            applied.add(ReservationHoldDiscountEntity.builder()
                    .hold(hold)
                    .discount(discount)
                    .discountCode(summary.getCode())
                    .amount(summary.getAmount())
                    .autoApplied(summary.isAutoApplied())
                    .stackRank(i)
                    .build());
        }
        return applied;
    }

    private List<HoldResponse.AppliedDiscountInfo> mapAppliedDiscounts(ReservationHoldEntity hold) {
        if (hold.getAppliedDiscounts() == null) {
            return List.of();
        }
        return hold.getAppliedDiscounts().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getStackRank() != null ? a.getStackRank() : Integer.MAX_VALUE,
                        b.getStackRank() != null ? b.getStackRank() : Integer.MAX_VALUE))
                .map(applied -> HoldResponse.AppliedDiscountInfo.builder()
                        .discountId(applied.getDiscount().getId())
                        .code(applied.getDiscountCode())
                        .name(applied.getDiscount().getName())
                        .amount(applied.getAmount())
                        .autoApplied(applied.isAutoApplied())
                        .build())
                .collect(Collectors.toList());
    }
}
