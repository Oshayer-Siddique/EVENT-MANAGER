package com.oshayer.event_manager.ticketing.service.impl;

import com.oshayer.event_manager.events.entity.EventSeatEntity;
import com.oshayer.event_manager.events.entity.EventSeatEntity.EventSeatStatus;
import com.oshayer.event_manager.events.repository.EventRepository;
import com.oshayer.event_manager.events.repository.EventSeatRepository;
import com.oshayer.event_manager.ticketing.dto.HoldConvertRequest;
import com.oshayer.event_manager.ticketing.dto.HoldCreateRequest;
import com.oshayer.event_manager.ticketing.dto.HoldReleaseRequest;
import com.oshayer.event_manager.ticketing.dto.HoldResponse;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity.HoldStatus;
import com.oshayer.event_manager.ticketing.repository.ReservationHoldRepository;
import com.oshayer.event_manager.ticketing.service.ReservationHoldService;
import com.oshayer.event_manager.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationHoldServiceImpl implements ReservationHoldService {

    private final ReservationHoldRepository holdRepo;
    private final EventSeatRepository eventSeatRepo;
    private final EventRepository eventRepo;
    private final UserRepository userRepo;

    @Override
    public HoldResponse create(HoldCreateRequest req) {
        if (req.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new IllegalArgumentException("expiresAt must be in the future");

        var event = eventRepo.findById(req.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        List<EventSeatEntity> seatsToHold = new ArrayList<>();
        for (UUID seatId : req.getSeatIds()) {
            EventSeatEntity seat = eventSeatRepo.findByEventIdAndSeatId(req.getEventId(), seatId)
                    .orElseThrow(() -> new EntityNotFoundException("Seat with ID " + seatId + " not found for this event"));

            if (seat.getStatus() != EventSeatStatus.AVAILABLE) {
                throw new IllegalStateException("Seat " + seat.getSeat().getLabel() + " is not available.");
            }
            seatsToHold.add(seat);
        }

        // Mark all seats as RESERVED
        for (EventSeatEntity seat : seatsToHold) {
            seat.setStatus(EventSeatStatus.RESERVED);
        }

        var holdBuilder = ReservationHoldEntity.builder()
                .event(event)
                .status(HoldStatus.ACTIVE)
                .heldSeats(seatsToHold)
                .itemsJson(buildItemsJson(seatsToHold))
                .expiresAt(req.getExpiresAt());

        if (req.getBuyerId() != null) {
            var buyer = userRepo.findById(req.getBuyerId())
                    .orElseThrow(() -> new EntityNotFoundException("Buyer not found"));
            holdBuilder.buyer(buyer);
        }

        ReservationHoldEntity h = holdRepo.save(holdBuilder.build());
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
                .build();
    }

    private String buildItemsJson(List<EventSeatEntity> seats) {
        return seats.stream()
                .map(es -> "{" +
                        "\"seatId\":\"" + es.getId() + "\"," +
                        "\"seatLabel\":\"" + escapeJson(es.getSeat().getLabel()) + "\"," +
                        "\"tierCode\":\"" + escapeJson(es.getTierCode()) + "\"" +
                        "}")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
