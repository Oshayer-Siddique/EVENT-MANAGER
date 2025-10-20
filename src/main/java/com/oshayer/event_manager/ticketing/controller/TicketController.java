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

    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<TicketResponse>> listByEvent(@PathVariable java.util.UUID eventId) {
        return ResponseEntity.ok(ticketService.listByEvent(eventId));
    }
}
