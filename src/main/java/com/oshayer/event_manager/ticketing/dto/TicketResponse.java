package com.oshayer.event_manager.ticketing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private UUID id;
    private String status;

    // Flattened from related entities for convenience
    private UUID eventId;
    private UUID buyerId;
    private UUID seatId;
    private String seatLabel;
    private String tierCode;
    private BigDecimal price;

    // Verification and Holder Info
    private String qrCode;
    private String verificationCode;
    private String holderName;
    private String holderEmail;

    // Check-in Info
    private String gate;
    private UUID checkerId;
    private OffsetDateTime checkedInAt;

    // Lifecycle Timestamps
    private OffsetDateTime reservedUntil;
    private OffsetDateTime issuedAt;

    // Refund Info
    private BigDecimal refundAmount;
    private OffsetDateTime refundedAt;

    // Audit Timestamps
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

