package com.oshayer.event_manager.admin.service;

import com.oshayer.event_manager.admin.dto.*;

import java.util.List;

public interface AdminDashboardService {
    AdminOverviewResponse getOverview(DashboardFilter filter);
    List<EventPerformanceRow> getEventPerformance(DashboardFilter filter, int limit);
    List<SalesTrendPoint> getSalesTrend(DashboardFilter filter);
    OperationsSummaryResponse getOperations(DashboardFilter filter);
}
