package com.oshayer.event_manager.discounts.dto;

import com.oshayer.event_manager.discounts.entity.DiscountEntity;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class DiscountCalculationResult {
    BigDecimal subtotal;
    BigDecimal discountTotal;
    BigDecimal totalDue;
    List<DiscountValidationResponseItem> appliedDiscounts;
    List<DiscountEntity> appliedDiscountEntities;
}
