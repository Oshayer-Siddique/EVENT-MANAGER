package com.oshayer.event_manager.ticketing.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldCreateRequest {
    @NotNull private UUID eventId;
    private UUID buyerId; // nullable = guest allowed
    @NotBlank private String itemsJson;
    @Future @NotNull private OffsetDateTime expiresAt;
}
