package com.oshayer.event_manager.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EventTicketTierResponse {
    private UUID id;
    private String tierCode;
    private String tierName;
    private Integer totalQuantity;
    private BigDecimal price;
    private BigDecimal cost;
    private Boolean visible;
    private Integer soldQuantity;
    private Integer usedQuantity;
}
