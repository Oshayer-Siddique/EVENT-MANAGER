package com.oshayer.event_manager.ticketing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    private List<UUID> seatIds; // The specific seats to be held

    @Valid
    private List<TierSelection> tierSelections; // Optional for GA tiers

    @Future @NotNull private OffsetDateTime expiresAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TierSelection {
        @NotBlank private String tierCode;
        @NotNull @Min(1) private Integer quantity;
    }
}
