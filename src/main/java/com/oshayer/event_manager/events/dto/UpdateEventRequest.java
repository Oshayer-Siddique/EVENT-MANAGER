package com.oshayer.event_manager.events.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateEventRequest {
    // Identity & schedule
    private String typeCode;
    private String typeName;
    private String eventCode;   // unique if provided
    private String eventName;

    private ZonedDateTime eventStart;
    private ZonedDateTime eventEnd;

    // Venue & layout
    private UUID venueId;
    private UUID seatLayoutId;

    // Staffing
    private UUID eventManager;
    private UUID eventOperator1;
    private UUID eventOperator2;
    private UUID eventChecker1;
    private UUID eventChecker2;

    private List<UUID> organizerIds;
    private List<String> imageUrls;

    private List<UpdateEventTicketTierRequest> ticketTiers;

    // Replace associations if present (null = don’t touch, empty list = clear)
    private List<UUID> artistIds;
    private List<UUID> sponsorIds;

}
