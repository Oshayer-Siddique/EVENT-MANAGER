package com.oshayer.event_manager.ticketing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCreateRequest {
    @NotNull private UUID eventId;
    @NotNull private UUID seatId; // The ID of the desired SeatEntity
    @NotNull private UUID buyerId;

    // Optional reservation time for pending tickets
    private OffsetDateTime reservedUntil;

    // Optional holder info if different from buyer
    private String holderName;
    @Email private String holderEmail;
}

