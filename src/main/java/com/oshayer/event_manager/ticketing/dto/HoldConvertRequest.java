package com.oshayer.event_manager.ticketing.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldConvertRequest {
    @NotNull private UUID holdId;
    @NotNull private UUID paymentId;
}
