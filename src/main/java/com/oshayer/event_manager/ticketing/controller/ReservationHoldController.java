package com.oshayer.event_manager.ticketing.controller;

import com.oshayer.event_manager.ticketing.dto.*;
import com.oshayer.event_manager.ticketing.service.ReservationHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/holds")
@RequiredArgsConstructor
public class ReservationHoldController {

    private final ReservationHoldService holdService;

    @PostMapping
    public ResponseEntity<HoldResponse> create(@Valid @RequestBody HoldCreateRequest req) {
        return ResponseEntity.ok(holdService.create(req));
    }

    @PostMapping("/release")
    public ResponseEntity<HoldResponse> release(@Valid @RequestBody HoldReleaseRequest req) {
        return ResponseEntity.ok(holdService.release(req));
    }

    @PostMapping("/convert")
    public ResponseEntity<HoldResponse> convert(@Valid @RequestBody HoldConvertRequest req) {
        return ResponseEntity.ok(holdService.convert(req));
    }

    @GetMapping("/{holdId}")
    public ResponseEntity<HoldResponse> get(@PathVariable java.util.UUID holdId) {
        return ResponseEntity.ok(holdService.get(holdId));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<HoldResponse>> listActive(@PathVariable java.util.UUID eventId) {
        return ResponseEntity.ok(holdService.listActive(eventId));
    }
}
