package com.oshayer.event_manager.admin.dto;

import com.oshayer.event_manager.events.entity.EventSeatEntity;

public record SeatStatusSummary(
        EventSeatEntity.EventSeatStatus status,
        long count
) {}
