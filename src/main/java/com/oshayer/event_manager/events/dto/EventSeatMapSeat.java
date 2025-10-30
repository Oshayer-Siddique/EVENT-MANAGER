package com.oshayer.event_manager.events.dto;

import com.oshayer.event_manager.events.entity.EventSeatEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record EventSeatMapSeat(
        UUID seatId,
        UUID eventSeatId,
        String row,
        Integer number,
        String label,
        String type,
        String tierCode,
        BigDecimal price,
        EventSeatEntity.EventSeatStatus status
) {
}
