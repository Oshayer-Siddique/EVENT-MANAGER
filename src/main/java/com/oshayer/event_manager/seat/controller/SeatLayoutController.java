package com.oshayer.event_manager.seat.controller;

import com.oshayer.event_manager.seat.dto.BanquetLayoutDTO;
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

    @PutMapping("/venues/{venueId}/layouts/{layoutId}")
    public ResponseEntity<SeatLayoutDTO> updateForVenue(
            @PathVariable UUID venueId,
            @PathVariable UUID layoutId,
            @Valid @RequestBody SeatLayoutDTO dto) {
        dto.setId(layoutId);
        dto.setVenueId(venueId);
        return ResponseEntity.ok(seatLayoutService.updateSeatLayout(venueId, layoutId, dto));
    }

    @DeleteMapping("/venues/{venueId}/layouts/{layoutId}")
    public ResponseEntity<Void> deleteForVenue(
            @PathVariable UUID venueId,
            @PathVariable UUID layoutId) {
        seatLayoutService.deleteSeatLayout(venueId, layoutId);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/seat-layouts/{id}/banquet")
    public ResponseEntity<BanquetLayoutDTO> getBanquetLayout(@PathVariable UUID id) {
        return ResponseEntity.ok(seatLayoutService.getBanquetLayout(id));
    }

    @PutMapping("/seat-layouts/{id}/banquet")
    public ResponseEntity<BanquetLayoutDTO> updateBanquetLayout(
            @PathVariable UUID id,
            @RequestBody BanquetLayoutDTO layoutDTO) {
        return ResponseEntity.ok(seatLayoutService.updateBanquetLayout(id, layoutDTO));
    }
}
