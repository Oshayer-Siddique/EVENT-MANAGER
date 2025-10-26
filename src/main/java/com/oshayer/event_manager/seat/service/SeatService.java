package com.oshayer.event_manager.seat.service;

import com.oshayer.event_manager.seat.dto.SeatDTO;

import java.util.List;
import java.util.UUID;

public interface SeatService {

    List<SeatDTO> getSeats(UUID layoutId);

    SeatDTO createSeat(UUID layoutId, SeatDTO dto);

    SeatDTO updateSeat(UUID layoutId, UUID seatId, SeatDTO dto);

    void deleteSeat(UUID layoutId, UUID seatId);
}

