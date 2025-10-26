package com.oshayer.event_manager.events.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateEventTicketTierRequest {
    private UUID id; // ID is needed to identify which tier to update
    private String tierCode;
    private String tierName;
    @PositiveOrZero private Integer totalQuantity;
    @DecimalMin(value = "0.0", inclusive = true) private BigDecimal price;
    @DecimalMin(value = "0.0", inclusive = true) private BigDecimal cost;
    private Boolean visible;
}
