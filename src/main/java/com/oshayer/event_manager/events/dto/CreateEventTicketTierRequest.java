package com.oshayer.event_manager.events.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateEventTicketTierRequest {
    @NotBlank private String tierCode;
    @NotBlank private String tierName;
    @NotNull @PositiveOrZero private Integer totalQuantity;
    @NotNull @DecimalMin(value = "0.0", inclusive = true) private BigDecimal price;
    @NotNull @DecimalMin(value = "0.0", inclusive = true) private BigDecimal cost;
    @Builder.Default private Boolean visible = Boolean.TRUE;
}
