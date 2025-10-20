package com.oshayer.event_manager.ticketing.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRefundRequest {
    @NotNull @DecimalMin("0.00")
    private BigDecimal refundAmount;
}

