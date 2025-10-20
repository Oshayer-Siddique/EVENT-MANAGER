package com.oshayer.event_manager.ticketing.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldReleaseRequest {
    @NotNull private UUID holdId;
    private String reason;
}
