package com.oshayer.event_manager.admin.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Value
@Builder
public class DashboardFilter {
    OffsetDateTime from;
    OffsetDateTime to;
    UUID eventId;
    UUID venueId;
    Duration expiringWithin;

    public OffsetDateTime resolveFrom() {
        return Optional.ofNullable(from).orElseGet(() -> OffsetDateTime.now().minusDays(30));
    }

    public OffsetDateTime resolveTo() {
        return Optional.ofNullable(to).orElseGet(OffsetDateTime::now);
    }

    public ZonedDateTime resolveFromZoned() {
        return resolveFrom().atZoneSameInstant(ZoneId.systemDefault());
    }

    public ZonedDateTime resolveToZoned() {
        return resolveTo().atZoneSameInstant(ZoneId.systemDefault());
    }

    public Duration resolveExpiringWithin() {
        return Optional.ofNullable(expiringWithin).orElse(Duration.ofMinutes(15));
    }
}
