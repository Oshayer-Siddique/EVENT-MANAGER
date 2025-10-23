package com.oshayer.event_manager.events.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateEventRequest {

    // Identity & schedule
    @NotBlank private String typeCode;
    @NotBlank private String typeName;
    @NotBlank private String eventCode;
    @NotBlank private String eventName;
    @NotNull private ZonedDateTime eventStart;
    @NotNull private ZonedDateTime eventEnd;

    // Venue & layout
    @NotNull private UUID venueId;
    private UUID seatLayoutId; // optional

    // Staffing
    @NotNull private UUID eventManager;
    @NotNull private UUID eventOperator1;
    private UUID eventOperator2;
    @NotNull private UUID eventChecker1;
    private UUID eventChecker2;

    private List<String> imageUrls;

    private List<CreateEventTicketTierRequest> ticketTiers;

    // Optional associations
    private List<UUID> artistIds;
    private List<UUID> sponsorIds;
    private List<UUID> organizerIds;


}
