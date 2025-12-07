package com.oshayer.event_manager.discounts.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class DiscountCalculationRequest {
    @NotNull UUID eventId;
    UUID buyerId;
    String discountCode;
    boolean includeAutomaticDiscounts;
    @Valid @NotEmpty List<DiscountLineItem> items;
}
