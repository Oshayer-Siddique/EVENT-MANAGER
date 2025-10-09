package com.oshayer.event_manager.seat.controller;

import com.oshayer.event_manager.seat.dto.SeatLayoutDTO;
import com.oshayer.event_manager.seat.service.SeatLayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SeatLayoutController {

    private final SeatLayoutService seatLayoutService;

    // -------- venue-scoped routes (recommended) --------

    @PostMapping("/venues/{venueId}/layouts")
    public ResponseEntity<SeatLayoutDTO> createForVenue(
            @PathVariable UUID venueId,
            @Valid @RequestBody SeatLayoutDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seatLayoutService.createSeatLayout(venueId, dto));
    }

    @GetMapping("/venues/{venueId}/layouts")
    public ResponseEntity<List<SeatLayoutDTO>> listForVenue(@PathVariable UUID venueId) {
        return ResponseEntity.ok(seatLayoutService.getSeatLayoutsByVenue(venueId));
    }

    // -------- id-scoped routes (generic) --------

    @GetMapping("/seat-layouts/{id}")
    public ResponseEntity<SeatLayoutDTO> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(seatLayoutService.getSeatLayout(id));
    }

    @PutMapping("/seat-layouts/{id}")
    public ResponseEntity<SeatLayoutDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody SeatLayoutDTO dto) {
        return ResponseEntity.ok(seatLayoutService.updateSeatLayout(id, dto));
    }

    @DeleteMapping("/seat-layouts/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        seatLayoutService.deleteSeatLayout(id);
        return ResponseEntity.noContent().build();
    }
}
