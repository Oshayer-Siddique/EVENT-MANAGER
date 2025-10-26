package com.oshayer.event_manager.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesTrendPoint(
        LocalDate date,
        long ticketsIssued,
        BigDecimal grossRevenue,
        BigDecimal refundTotal,
        BigDecimal netRevenue
) {}
