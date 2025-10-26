package com.oshayer.event_manager.admin.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record EventPerformanceRow(
        UUID eventId,
        String eventName,
        ZonedDateTime eventStart,
        long ticketsSold,
        long capacity,
        BigDecimal grossRevenue,
        BigDecimal refundedAmount,
        BigDecimal netRevenue,
        double sellThrough,
        long availableSeats
) {}
