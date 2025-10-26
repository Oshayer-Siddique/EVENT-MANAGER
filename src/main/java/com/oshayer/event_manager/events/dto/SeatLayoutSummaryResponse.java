package com.oshayer.event_manager.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayoutSummaryResponse {
    private UUID id;
    private String typeCode;
    private String typeName;
    private String layoutName;
    private Integer totalRows;
    private Integer totalCols;
    private Integer totalTables;
    private Integer chairsPerTable;
    private Integer standingCapacity;
    private Integer totalCapacity;
    private Boolean active;
}
