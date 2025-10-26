package com.oshayer.event_manager.admin.dto;

import com.oshayer.event_manager.ticketing.entity.TicketEntity;

import java.math.BigDecimal;

public record TicketStatusSummary(
        TicketEntity.TicketStatus status,
        long count,
        BigDecimal gross,
        BigDecimal refunded
) {
    public BigDecimal net() {
        return gross.subtract(refunded);
    }
}
