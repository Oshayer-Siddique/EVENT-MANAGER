package com.oshayer.event_manager.ticketing.service.impl;

import com.oshayer.event_manager.events.entity.EventSeatEntity;
import com.oshayer.event_manager.events.entity.EventSeatEntity.EventSeatStatus;
import com.oshayer.event_manager.events.repository.EventSeatRepository;
import com.oshayer.event_manager.events.repository.EventTicketTierRepository;
import com.oshayer.event_manager.ticketing.dto.TicketCheckInRequest;
import com.oshayer.event_manager.ticketing.dto.TicketCreateRequest;
import com.oshayer.event_manager.ticketing.dto.TicketRefundRequest;
import com.oshayer.event_manager.ticketing.dto.TicketResponse;
import com.oshayer.event_manager.ticketing.entity.TicketEntity;
import com.oshayer.event_manager.ticketing.entity.TicketEntity.TicketStatus;
import com.oshayer.event_manager.ticketing.repository.TicketRepository;
import com.oshayer.event_manager.ticketing.service.TicketService;
import com.oshayer.event_manager.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final EventSeatRepository eventSeatRepository;
    private final EventTicketTierRepository eventTicketTierRepository;
    private final UserRepository userRepository;

    @Override
    public TicketResponse createPending(TicketCreateRequest req) {
        // 1. Find the requested EventSeat
        EventSeatEntity eventSeat = eventSeatRepository.findByEventIdAndSeatId(req.getEventId(), req.getSeatId())
                .orElseThrow(() -> new EntityNotFoundException("Event seat not found"));

        // 2. Check if it's available
        if (eventSeat.getStatus() != EventSeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat " + eventSeat.getSeat().getLabel() + " is not available");
        }

        // 3. Find the buyer
        var buyer = userRepository.findById(req.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found"));

        // 4. Mark the seat as reserved
        eventSeat.setStatus(EventSeatStatus.RESERVED);
        eventSeatRepository.save(eventSeat);

        // 5. Create the ticket
        String qr = UUID.randomUUID().toString();
        String shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        TicketEntity t = TicketEntity.builder()
                .eventSeat(eventSeat)
                .buyer(buyer)
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
            // Revert the seat status to AVAILABLE
            t.getEventSeat().setStatus(EventSeatStatus.AVAILABLE);
            return toResponse(t);
        }
        t.setStatus(TicketStatus.ISSUED);
        t.setIssuedAt(OffsetDateTime.now());
        // Mark the seat as permanently SOLD
        t.getEventSeat().setStatus(EventSeatStatus.SOLD);
        adjustTierSoldCount(t.getEventSeat(), 1);
        return toResponse(t);
    }

    @Override
    public TicketResponse checkIn(UUID ticketId, TicketCheckInRequest req) {
        TicketEntity t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        if (t.getStatus() != TicketStatus.ISSUED)
            throw new IllegalStateException("Only ISSUED tickets can be checked in");

        var checker = userRepository.findById(req.getCheckerId())
                .orElseThrow(() -> new EntityNotFoundException("Checker not found"));

        t.setStatus(TicketStatus.USED);
        t.setCheckedInAt(OffsetDateTime.now());
        t.setChecker(checker);
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

        // Business Decision: When a ticket is refunded, the seat should become available again.
        t.getEventSeat().setStatus(EventSeatStatus.AVAILABLE);
        adjustTierSoldCount(t.getEventSeat(), -1);

        t.setStatus(TicketStatus.REFUNDED);
        t.setRefundAmount(req.getRefundAmount());
        t.setRefundedAt(OffsetDateTime.now());

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
        return ticketRepository.findByEventSeat_Event_Id(eventId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> listByBuyer(UUID buyerId) {
        return ticketRepository.findByBuyer_Id(buyerId).stream().map(this::toResponse).toList();
    }

    // -------- helpers --------

    private TicketResponse toResponse(TicketEntity t) {
        EventSeatEntity es = t.getEventSeat();
        return TicketResponse.builder()
                .id(t.getId())
                .status(t.getStatus().name())
                // Flattened data from related entities
                .eventId(es.getEvent().getId())
                .buyerId(t.getBuyer().getId())
                .seatId(es.getSeat().getId())
                .seatLabel(es.getSeat().getLabel())
                .tierCode(es.getTierCode())
                .price(es.getPrice())
                // Core ticket info
                .qrCode(t.getQrCode())
                .verificationCode(t.getVerificationCode())
                .holderName(t.getHolderName())
                .holderEmail(t.getHolderEmail())
                .gate(t.getGate())
                .checkerId(t.getChecker() != null ? t.getChecker().getId() : null)
                .reservedUntil(t.getReservedUntil())
                .issuedAt(t.getIssuedAt())
                .checkedInAt(t.getCheckedInAt())
                .refundAmount(t.getRefundAmount())
                .refundedAt(t.getRefundedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private void adjustTierSoldCount(EventSeatEntity seat, int delta) {
        if (seat == null || delta == 0) {
            return;
        }

        var event = seat.getEvent();
        if (event == null) {
            return;
        }

        eventTicketTierRepository.findByEventIdAndTierCode(event.getId(), seat.getTierCode())
                .ifPresent(tier -> {
                    int current = tier.getSoldQuantity() == null ? 0 : tier.getSoldQuantity();
                    int updated = Math.max(0, current + delta);
                    tier.setSoldQuantity(updated);
                });
    }
}
