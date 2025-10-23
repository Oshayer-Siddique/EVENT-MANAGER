package com.oshayer.event_manager.ticketing.controller;

import com.oshayer.event_manager.ticketing.dto.*;
import com.oshayer.event_manager.ticketing.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/reserve")
    public ResponseEntity<TicketResponse> reserve(@Valid @RequestBody TicketCreateRequest req) {
        return ResponseEntity.ok(ticketService.createPending(req));
    }

    @PostMapping("/issue/{ticketId}")
    public ResponseEntity<TicketResponse> issue(@PathVariable java.util.UUID ticketId) {
        return ResponseEntity.ok(ticketService.issue(ticketId));
    }

    @PostMapping("/checkin/{ticketId}")
    public ResponseEntity<TicketResponse> checkIn(@PathVariable java.util.UUID ticketId,
                                                  @Valid @RequestBody TicketCheckInRequest req) {
        return ResponseEntity.ok(ticketService.checkIn(ticketId, req));
    }

    @PostMapping("/refund/{ticketId}")
    public ResponseEntity<TicketResponse> refund(@PathVariable java.util.UUID ticketId,
                                                 @Valid @RequestBody TicketRefundRequest req) {
        return ResponseEntity.ok(ticketService.refund(ticketId, req));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> get(@PathVariable java.util.UUID ticketId) {
        return ResponseEntity.ok(ticketService.get(ticketId));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> listTickets(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) UUID buyerId
    ) {
        if (eventId != null) {
            return ResponseEntity.ok(ticketService.listByEvent(eventId));
        }
        if (buyerId != null) {
            return ResponseEntity.ok(ticketService.listByBuyer(buyerId));
        }
        // Consider returning a 400 Bad Request if no filter is provided, or defaulting to all tickets if that's desired.
        return ResponseEntity.ok(Collections.emptyList());
    }
}
