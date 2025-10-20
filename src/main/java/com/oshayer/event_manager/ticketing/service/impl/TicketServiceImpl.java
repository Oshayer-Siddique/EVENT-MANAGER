package com.oshayer.event_manager.ticketing.service.impl;

import com.oshayer.event_manager.events.entity.EventEntity;
import com.oshayer.event_manager.events.repository.EventRepository;
import com.oshayer.event_manager.ticketing.dto.*;
import com.oshayer.event_manager.ticketing.entity.TicketEntity;
import com.oshayer.event_manager.ticketing.entity.TicketEntity.TicketStatus;
import com.oshayer.event_manager.ticketing.repository.TicketRepository;
import com.oshayer.event_manager.ticketing.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    private static final Set<TicketStatus> SOLD_STATUSES = EnumSet.of(
            TicketStatus.PENDING, TicketStatus.ISSUED, TicketStatus.USED, TicketStatus.REFUNDED
    );

    @Override
    public TicketResponse createPending(TicketCreateRequest req) {
        EventEntity event = eventRepository.findById(req.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // seat conflict check (if seatLabel provided)
        if (req.getSeatLabel() != null && !req.getSeatLabel().isBlank()) {
            boolean seatTaken = ticketRepository.existsByEventIdAndSeatLabelAndStatusIn(
                    req.getEventId(), req.getSeatLabel(), SOLD_STATUSES);
            if (seatTaken) throw new IllegalStateException("Seat already taken for this event");
        }

        // capacity guard
        assertCapacityAvailable(event, req.getTierCode(), 1);

        // tokens
        String qr = UUID.randomUUID().toString();
        String shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        TicketEntity t = TicketEntity.builder()
                .eventId(req.getEventId())
                .buyerId(req.getBuyerId())
                .seatLayoutId(req.getSeatLayoutId())
                .seatLabel(req.getSeatLabel())
                .tierCode(req.getTierCode().trim().toUpperCase())
                .currency(req.getCurrency())
                .price(req.getPrice())
                .status(TicketStatus.PENDING)
                .reservedUntil(req.getReservedUntil())
                .holderName(req.getHolderName())
                .holderEmail(req.getHolderEmail())
                .qrCode(qr)
                .verificationCode(shortCode)
                .build();

        t = ticketRepository.save(t);
        return toResponse(t);
    }

    @Override
    public TicketResponse issue(UUID ticketId) {
        TicketEntity t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        if (t.getStatus() != TicketStatus.PENDING)
            throw new IllegalStateException("Only PENDING tickets can be issued");
        if (t.getReservedUntil() != null && t.getReservedUntil().isBefore(OffsetDateTime.now())) {
            t.setStatus(TicketStatus.EXPIRED);
            return toResponse(t);
        }
        t.setStatus(TicketStatus.ISSUED);
        t.setIssuedAt(OffsetDateTime.now());
        return toResponse(t);
    }

    @Override
    public TicketResponse checkIn(UUID ticketId, TicketCheckInRequest req) {
        TicketEntity t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        if (t.getStatus() != TicketStatus.ISSUED)
            throw new IllegalStateException("Only ISSUED tickets can be checked in");
        t.setStatus(TicketStatus.USED);
        t.setCheckedInAt(OffsetDateTime.now());
        t.setCheckerId(req.getCheckerId());
        t.setGate(req.getGate());
        return toResponse(t);
    }

    @Override
    public TicketResponse refund(UUID ticketId, TicketRefundRequest req) {
        TicketEntity t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        switch (t.getStatus()) {
            case PENDING, ISSUED, USED -> { /* refundable per business rules */ }
            default -> throw new IllegalStateException("Ticket not refundable in status: " + t.getStatus());
        }

        t.setStatus(TicketStatus.REFUNDED);
        t.setRefundAmount(req.getRefundAmount());
        t.setRefundedAt(OffsetDateTime.now()); // ✅ correct version

        return toResponse(t);
    }


    @Override
    @Transactional(readOnly = true)
    public TicketResponse get(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> listByEvent(UUID eventId) {
        return ticketRepository.findByEventId(eventId).stream().map(this::toResponse).toList();
    }

    // -------- helpers --------
    private void assertCapacityAvailable(EventEntity event, String tierCode, int qty) {
        String upper = tierCode == null ? null : tierCode.trim().toUpperCase();
        int capacity;
        switch (upper) {
            case "VIP" -> capacity = n(event.getVipTickets());
            case "PLAT", "PLATINUM" -> capacity = n(event.getPlatTickets());
            case "GOLD" -> capacity = n(event.getGoldTickets());
            case "SILVER" -> capacity = n(event.getSilverTickets());
            default -> throw new IllegalArgumentException("Unknown tierCode: " + tierCode);
        }
        long sold = ticketRepository.countByEventIdAndTierCodeAndStatusIn(event.getId(), upper, SOLD_STATUSES);
        if (sold + qty > capacity) {
            throw new IllegalStateException("Capacity exceeded for tier " + upper + ": " + (sold + qty) + "/" + capacity);
        }
    }

    private int n(Integer x) { return x == null ? 0 : x; }

    private TicketResponse toResponse(TicketEntity t) {
        return TicketResponse.builder()
                .id(t.getId())
                .eventId(t.getEventId())
                .buyerId(t.getBuyerId())
                .seatLayoutId(t.getSeatLayoutId())
                .seatLabel(t.getSeatLabel())
                .tierCode(t.getTierCode())
                .currency(t.getCurrency())
                .price(t.getPrice())
                .status(t.getStatus().name())
                .qrCode(t.getQrCode())
                .verificationCode(t.getVerificationCode())
                .holderName(t.getHolderName())
                .holderEmail(t.getHolderEmail())
                .gate(t.getGate())
                .checkerId(t.getCheckerId())
                .reservedUntil(t.getReservedUntil())
                .issuedAt(t.getIssuedAt())
                .checkedInAt(t.getCheckedInAt())
                .refundAmount(t.getRefundAmount())
                .refundedAt(t.getRefundedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
