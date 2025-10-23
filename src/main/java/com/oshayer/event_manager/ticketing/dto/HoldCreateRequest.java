package com.oshayer.event_manager.ticketing.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldCreateRequest {
    @NotNull private UUID eventId;
    private UUID buyerId; // nullable = guest allowed

    @NotEmpty
    private List<UUID> seatIds; // The specific seats to be held

    @Future @NotNull private OffsetDateTime expiresAt;
}

