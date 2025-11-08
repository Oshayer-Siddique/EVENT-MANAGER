package com.oshayer.event_manager.events.dto;

import com.oshayer.event_manager.events.entity.EventSeatEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SeatAssignmentUpdateRequest(
        @NotEmpty List<@Valid SeatAssignment> seats
) {

    public record SeatAssignment(
            UUID eventSeatId,
            UUID seatId,
            @NotBlank String tierCode,
            BigDecimal price,
            EventSeatEntity.EventSeatStatus status
    ) {
    }
}
