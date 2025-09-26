package com.oshayer.event_manager.seat.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayoutDTO {
    private UUID id;
    private String typeCode;
    private String typeName;
    private UUID venueId;
    private String layoutName;
    private Integer totalRows;
    private Integer totalCols;
    private Integer totalTables;
    private Integer chairsPerTable;
    private Integer standingCapacity;
    private Integer totalCapacity;
    private Boolean isActive;
}
