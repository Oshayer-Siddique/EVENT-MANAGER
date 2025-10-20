package com.oshayer.event_manager.ticketing.dto;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldResponse {
    private UUID id;
    private UUID eventId;
    private UUID buyerId;
    private String status;
    private String itemsJson;
    private OffsetDateTime expiresAt;
    private UUID finalizedPaymentId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
