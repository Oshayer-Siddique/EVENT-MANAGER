package com.oshayer.event_manager.admin.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HoldAlert(
        UUID holdId,
        UUID eventId,
        OffsetDateTime expiresAt,
        int seatCount
) {}
