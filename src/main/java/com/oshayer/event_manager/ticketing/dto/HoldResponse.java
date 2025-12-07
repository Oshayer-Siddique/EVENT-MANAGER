package com.oshayer.event_manager.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
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
    private List<HeldSeatInfo> heldSeats;
    private OffsetDateTime expiresAt;
    private UUID finalizedPaymentId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private List<AppliedDiscountInfo> appliedDiscounts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeldSeatInfo {
        private UUID seatId;
        private String seatLabel;
        private String tierCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppliedDiscountInfo {
        private UUID discountId;
        private String code;
        private String name;
        private BigDecimal amount;
        private boolean autoApplied;
    }
}
