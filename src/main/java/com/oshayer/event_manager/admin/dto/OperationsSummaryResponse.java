package com.oshayer.event_manager.admin.dto;

import java.util.List;

public record OperationsSummaryResponse(
        long activeHolds,
        long expiringSoon,
        long seatsSold,
        long seatsReserved,
        long seatsAvailable,
        List<HoldAlert> holdAlerts
) {}
