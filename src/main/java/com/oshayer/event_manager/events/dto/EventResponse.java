package com.oshayer.event_manager.events.dto;

import lombok.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EventResponse {
    private UUID id;

    private String typeCode;
    private String typeName;
    private String eventCode;
    private String eventName;
    private String eventDescription;
    private String privacyPolicy;
    private ZonedDateTime eventStart;
    private ZonedDateTime eventEnd;

    private UUID venueId;
    private String venueName;
    private UUID seatLayoutId;

    private UUID eventManager;
    private UUID eventOperator1;
    private UUID eventOperator2;
    private UUID eventChecker1;
    private UUID eventChecker2;

    private List<UUID> organizerIds;
    private List<String> imageUrls;

    private List<EventTicketTierResponse> ticketTiers;

    private List<UUID> artistIds;
    private List<UUID> sponsorIds;

}
