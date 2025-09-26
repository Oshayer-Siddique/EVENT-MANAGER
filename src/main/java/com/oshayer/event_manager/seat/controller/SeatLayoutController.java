package com.oshayer.event_manager.seat.controller;

import com.oshayer.event_manager.seat.dto.SeatLayoutDTO;
import com.oshayer.event_manager.seat.service.SeatLayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seat-layouts")
@RequiredArgsConstructor
public class SeatLayoutController {

    private final SeatLayoutService seatLayoutService;

    @PostMapping
    public ResponseEntity<SeatLayoutDTO> createLayout(@RequestBody SeatLayoutDTO dto) {
        return ResponseEntity.ok(seatLayoutService.createSeatLayout(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatLayoutDTO> getLayout(@PathVariable UUID id) {
        return ResponseEntity.ok(seatLayoutService.getSeatLayout(id));
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<SeatLayoutDTO>> getLayoutsByVenue(@PathVariable UUID venueId) {
        return ResponseEntity.ok(seatLayoutService.getSeatLayoutsByVenue(venueId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatLayoutDTO> updateLayout(@PathVariable UUID id, @RequestBody SeatLayoutDTO dto) {
        return ResponseEntity.ok(seatLayoutService.updateSeatLayout(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLayout(@PathVariable UUID id) {
        seatLayoutService.deleteSeatLayout(id);
        return ResponseEntity.noContent().build();
    }
}
