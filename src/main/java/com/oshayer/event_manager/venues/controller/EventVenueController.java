package com.oshayer.event_manager.venues.controller;

import com.oshayer.event_manager.venues.dto.EventVenueDTO;
import com.oshayer.event_manager.venues.service.EventVenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class EventVenueController {

    private final EventVenueService venueService;

    @PostMapping
    public ResponseEntity<EventVenueDTO> createVenue(@RequestBody EventVenueDTO dto) {
        return ResponseEntity.ok(venueService.createVenue(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventVenueDTO> getVenue(@PathVariable UUID id) {
        return ResponseEntity.ok(venueService.getVenue(id));
    }

    @GetMapping
    public ResponseEntity<List<EventVenueDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventVenueDTO> updateVenue(@PathVariable UUID id, @RequestBody EventVenueDTO dto) {
        return ResponseEntity.ok(venueService.updateVenue(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable UUID id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
