package com.oshayer.event_manager.events.controller;

import com.oshayer.event_manager.events.dto.CreateEventRequest;
import com.oshayer.event_manager.events.dto.EventResponse;
import com.oshayer.event_manager.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Create a new event.
     * - Validates DTO (Bean Validation)
     * - Enforces uniqueness, roles, venue/layout ownership in the service
     * - Returns 201 Created with Location: /api/events/{id}
     */
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest req) {
        EventResponse created = eventService.create(req);
        return ResponseEntity
                .created(URI.create("/api/events/" + created.getId()))
                .body(created);
    }
}
