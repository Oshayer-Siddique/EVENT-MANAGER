package com.oshayer.event_manager.seat.controller;

import com.oshayer.event_manager.seat.dto.SeatDTO;
import com.oshayer.event_manager.seat.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seat-layouts/{layoutId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping
    public ResponseEntity<List<SeatDTO>> list(@PathVariable UUID layoutId) {
        return ResponseEntity.ok(seatService.getSeats(layoutId));
    }

    @PostMapping
    public ResponseEntity<SeatDTO> create(@PathVariable UUID layoutId,
                                          @Valid @RequestBody SeatDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seatService.createSeat(layoutId, dto));
    }

    @PutMapping("/{seatId}")
    public ResponseEntity<SeatDTO> update(@PathVariable UUID layoutId,
                                          @PathVariable UUID seatId,
                                          @Valid @RequestBody SeatDTO dto) {
        return ResponseEntity.ok(seatService.updateSeat(layoutId, seatId, dto));
    }

    @DeleteMapping("/{seatId}")
    public ResponseEntity<Void> delete(@PathVariable UUID layoutId,
                                       @PathVariable UUID seatId) {
        seatService.deleteSeat(layoutId, seatId);
        return ResponseEntity.noContent().build();
    }
}

