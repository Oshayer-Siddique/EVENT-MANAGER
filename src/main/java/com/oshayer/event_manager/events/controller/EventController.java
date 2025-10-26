package com.oshayer.event_manager.events.controller;

import com.oshayer.event_manager.events.dto.CreateEventRequest;
import com.oshayer.event_manager.events.dto.EventResponse;
import com.oshayer.event_manager.events.dto.EventSeatResponse;
import com.oshayer.event_manager.events.dto.EventTicketDetailsResponse;
import com.oshayer.event_manager.events.dto.SeatInventorySyncRequest;
import com.oshayer.event_manager.events.dto.UpdateEventRequest;
import com.oshayer.event_manager.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // CREATE
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest req) {
        EventResponse created = eventService.create(req);
        return ResponseEntity
                .created(URI.create("/api/events/" + created.getId()))
                .body(created);
    }

    // GET (by id)
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.get(id));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<EventSeatResponse>> listSeats(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.listSeats(id));
    }

    @PostMapping("/{id}/seats/sync")
    public ResponseEntity<List<EventSeatResponse>> syncSeatInventory(
            @PathVariable UUID id,
            @Valid @RequestBody SeatInventorySyncRequest request) {
        return ResponseEntity.ok(eventService.syncSeatInventory(id, request));
    }

    // Tickets + assets
    @GetMapping("/{id}/ticket-details")
    public ResponseEntity<EventTicketDetailsResponse> getTicketDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getTicketDetails(id));
    }

    // LIST (paged)
    @GetMapping
    public ResponseEntity<Page<EventResponse>> list(
            @PageableDefault(size = 20, sort = "eventStart") Pageable pageable) {
        return ResponseEntity.ok(eventService.list(pageable));
    }

    // UPDATE (full/partial via PUT – your UpdateEventRequest is partial-friendly)
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest req) {
        return ResponseEntity.ok(eventService.update(id, req));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
