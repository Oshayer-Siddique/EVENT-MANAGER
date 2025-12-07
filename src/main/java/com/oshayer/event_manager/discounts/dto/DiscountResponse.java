package com.oshayer.event_manager.discounts.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class DiscountResponse {
    UUID id;
    String name;
    String code;
    DiscountValueTypeDto valueType;
    BigDecimal value;
    BigDecimal maxDiscountAmount;
    BigDecimal minimumOrderAmount;
    Integer maxRedemptions;
    Integer maxRedemptionsPerBuyer;
    OffsetDateTime startsAt;
    OffsetDateTime endsAt;
    UUID eventId;
    String tierCode;
    boolean autoApply;
    boolean stackable;
    boolean active;
    boolean allowGuestRedemption;
    int priority;
    String notes;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
