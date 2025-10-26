package com.oshayer.event_manager.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminOverviewResponse(
        EventMetrics events,
        TicketMetrics tickets,
        RevenueMetrics revenue,
        CustomerMetrics customers
) {
    public record EventMetrics(long total, long live, long upcoming, long completed) {}
    public record TicketMetrics(long pending, long issued, long used, long refunded, long canceled, long expired) {}
    public record RevenueMetrics(BigDecimal gross, BigDecimal refunded, BigDecimal net) {}
    public record CustomerMetrics(long newUsers, List<RoleCount> roleCounts) {}
    public record RoleCount(String role, long count) {}
}
