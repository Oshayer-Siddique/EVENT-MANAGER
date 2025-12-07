package com.oshayer.event_manager.discounts.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DiscountValidationRequest {

    @NotNull
    private UUID eventId;

    private UUID buyerId;

    private String discountCode;

    private boolean includeAutomaticDiscounts = true;

    @Valid
    @NotEmpty
    private List<DiscountLineItem> items;
}
