package com.oshayer.event_manager.seat.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayoutDTO {
    private UUID id;

    @NotBlank private String typeCode;
    @NotBlank private String typeName;

    // REMOVE @NotNull here because venueId comes from the path in /venues/{venueId}/layouts
    private UUID venueId;

    @NotBlank private String layoutName;

    @PositiveOrZero private Integer totalRows;
    @PositiveOrZero private Integer totalCols;
    @PositiveOrZero private Integer totalTables;
    @PositiveOrZero private Integer chairsPerTable;
    @PositiveOrZero private Integer standingCapacity;

    @NotNull @PositiveOrZero private Integer totalCapacity;
    @NotNull private Boolean isActive;
}

