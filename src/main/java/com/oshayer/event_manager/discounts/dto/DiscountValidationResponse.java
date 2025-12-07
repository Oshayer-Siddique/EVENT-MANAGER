package com.oshayer.event_manager.discounts.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class DiscountValidationResponse {
    BigDecimal subtotal;
    BigDecimal discountTotal;
    BigDecimal totalDue;
    List<DiscountValidationResponseItem> appliedDiscounts;
}
