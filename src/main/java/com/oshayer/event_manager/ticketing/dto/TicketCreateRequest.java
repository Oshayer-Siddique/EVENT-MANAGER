package com.oshayer.event_manager.ticketing.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCreateRequest {
    @NotNull private UUID eventId;
    @NotNull private UUID buyerId;
    @NotBlank private String tierCode; // VIP / PLAT / GOLD / SILVER

    private UUID seatLayoutId; // optional
    private String seatLabel;  // optional

    @NotBlank private String currency;
    @NotNull @DecimalMin("0.00") private BigDecimal price;

    private OffsetDateTime reservedUntil; // optional direct reserve
    private String holderName;
    @Email private String holderEmail;
}
