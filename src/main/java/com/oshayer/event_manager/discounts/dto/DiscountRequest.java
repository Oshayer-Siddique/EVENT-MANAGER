package com.oshayer.event_manager.discounts.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class DiscountRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @NotNull
    private DiscountValueTypeDto valueType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal value;

    private BigDecimal maxDiscountAmount;
    private BigDecimal minimumOrderAmount;

    @Min(0)
    private Integer maxRedemptions;

    @Min(0)
    private Integer maxRedemptionsPerBuyer;

    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;

    private UUID eventId;
    private String tierCode;

    private boolean autoApply;
    private boolean stackable;
    private boolean active = true;
    private boolean allowGuestRedemption;
    private Integer priority;
    private String notes;
}
