package com.oshayer.event_manager.ticketing.service.impl;

import com.oshayer.event_manager.ticketing.dto.*;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity.HoldStatus;
import com.oshayer.event_manager.ticketing.entity.TicketEntity;
import com.oshayer.event_manager.ticketing.entity.TicketEntity.TicketStatus;
import com.oshayer.event_manager.ticketing.repository.ReservationHoldRepository;
import com.oshayer.event_manager.ticketing.repository.TicketRepository;
import com.oshayer.event_manager.ticketing.service.ReservationHoldService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationHoldServiceImpl implements ReservationHoldService {

    private final ReservationHoldRepository holdRepo;
    private final TicketRepository ticketRepo;

    private static final Set<TicketStatus> SOLD_STATUSES = EnumSet.of(
            TicketStatus.PENDING, TicketStatus.ISSUED, TicketStatus.USED, TicketStatus.REFUNDED
    );

    @Override
    public HoldResponse create(HoldCreateRequest req) {
        if (req.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new IllegalArgumentException("expiresAt must be in the future");

        // Soft guard: seats in itemsJson should not already be taken by tickets or active holds.
        // (Deep validation of itemsJson can be added here by parsing JSON to extract seat labels.)
        ReservationHoldEntity h = ReservationHoldEntity.builder()
                .eventId(req.getEventId())
                .buyerId(req.getBuyerId())
                .status(HoldStatus.ACTIVE)
                .itemsJson(req.getItemsJson())
                .expiresAt(req.getExpiresAt())
                .build();
        h = holdRepo.save(h);
        return toResponse(h);
    }

    @Override
    public HoldResponse release(HoldReleaseRequest req) {
        ReservationHoldEntity h = holdRepo.findById(req.getHoldId())
                .orElseThrow(() -> new EntityNotFoundException("Hold not found"));
        if (h.getStatus() != HoldStatus.ACTIVE) return toResponse(h);
        h.setStatus(HoldStatus.RELEASED);
        return toResponse(h);
    }

    @Override
    public HoldResponse convert(HoldConvertRequest req) {
        ReservationHoldEntity h = holdRepo.findById(req.getHoldId())
                .orElseThrow(() -> new EntityNotFoundException("Hold not found"));
        if (h.getStatus() != HoldStatus.ACTIVE)
            throw new IllegalStateException("Only ACTIVE holds can be converted");
        if (h.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new IllegalStateException("Hold has expired");

        h.setStatus(HoldStatus.CONVERTED);
        h.setFinalizedPaymentId(req.getPaymentId());
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
        return HoldResponse.builder()
                .id(h.getId())
                .eventId(h.getEventId())
                .buyerId(h.getBuyerId())
                .status(h.getStatus().name())
                .itemsJson(h.getItemsJson())
                .expiresAt(h.getExpiresAt())
                .finalizedPaymentId(h.getFinalizedPaymentId())
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .build();
    }
}
