package com.oshayer.event_manager.seat.service;

import com.oshayer.event_manager.seat.dto.SeatLayoutDTO;

import java.util.List;
import java.util.UUID;

public interface SeatLayoutService {
    SeatLayoutDTO createSeatLayout(SeatLayoutDTO dto);
    SeatLayoutDTO getSeatLayout(UUID id);
    List<SeatLayoutDTO> getSeatLayoutsByVenue(UUID venueId);
    SeatLayoutDTO updateSeatLayout(UUID id, SeatLayoutDTO dto);
    void deleteSeatLayout(UUID id);
}
