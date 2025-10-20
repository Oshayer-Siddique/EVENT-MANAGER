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
    private UUID eventId;
    private UUID buyerId;
    private UUID seatLayoutId;
    private String seatLabel;
    private String tierCode;
    private String currency;
    private BigDecimal price;
    private String status;
    private String qrCode;
    private String verificationCode;
    private String holderName;
    private String holderEmail;
    private String gate;
    private UUID checkerId;
    private OffsetDateTime reservedUntil;
    private OffsetDateTime issuedAt;
    private OffsetDateTime checkedInAt;
    private BigDecimal refundAmount;
    private OffsetDateTime refundedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
