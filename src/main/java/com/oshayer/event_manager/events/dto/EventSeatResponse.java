package com.oshayer.event_manager.events.dto;

import com.oshayer.event_manager.events.entity.EventSeatEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record EventSeatResponse(
        UUID eventSeatId,
        UUID seatId,
        String label,
        String row,
        Integer number,
        String type,
        String tierCode,
        BigDecimal price,
        EventSeatEntity.EventSeatStatus status
) {
}
