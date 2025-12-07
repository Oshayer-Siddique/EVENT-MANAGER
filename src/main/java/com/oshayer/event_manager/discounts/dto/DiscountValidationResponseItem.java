package com.oshayer.event_manager.discounts.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class DiscountValidationResponseItem {
    UUID discountId;
    String code;
    String name;
    BigDecimal amount;
    boolean autoApplied;
    boolean stackable;
}
