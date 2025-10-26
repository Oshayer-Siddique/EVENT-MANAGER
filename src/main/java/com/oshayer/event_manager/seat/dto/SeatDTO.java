package com.oshayer.event_manager.seat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {

    private UUID id;

    private UUID layoutId;

    @NotBlank
    private String row;

    @NotNull
    @Min(1)
    private Integer number;

    private String label;

    private String type;
}

