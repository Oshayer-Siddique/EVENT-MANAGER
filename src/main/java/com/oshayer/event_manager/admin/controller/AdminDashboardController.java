package com.oshayer.event_manager.admin.controller;

import com.oshayer.event_manager.admin.dto.*;
import com.oshayer.event_manager.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/overview")
    public AdminOverviewResponse overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) UUID venueId,
            @RequestParam(required = false, defaultValue = "PT15M") Duration expiringWithin
    ) {
        DashboardFilter filter = DashboardFilter.builder()
                .from(from)
                .to(to)
                .eventId(eventId)
                .venueId(venueId)
                .expiringWithin(expiringWithin)
                .build();
        return dashboardService.getOverview(filter);
    }

    @GetMapping("/events")
    public List<EventPerformanceRow> eventPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) UUID venueId,
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        DashboardFilter filter = DashboardFilter.builder()
                .from(from)
                .to(to)
                .venueId(venueId)
                .build();
        return dashboardService.getEventPerformance(filter, limit);
    }

    @GetMapping("/sales-trend")
    public List<SalesTrendPoint> salesTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) UUID eventId
    ) {
        DashboardFilter filter = DashboardFilter.builder()
                .from(from)
                .to(to)
                .eventId(eventId)
                .build();
        return dashboardService.getSalesTrend(filter);
    }

    @GetMapping("/operations")
    public OperationsSummaryResponse operations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false, defaultValue = "PT15M") Duration expiringWithin
    ) {
        DashboardFilter filter = DashboardFilter.builder()
                .from(from)
                .to(to)
                .eventId(eventId)
                .expiringWithin(expiringWithin)
                .build();
        return dashboardService.getOperations(filter);
    }
}
