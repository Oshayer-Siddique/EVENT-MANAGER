package com.oshayer.event_manager.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatInventorySyncRequest {
    private String tierCode;
    private BigDecimal price;
    private Boolean overwriteExisting;
    private Boolean removeMissing;
}

