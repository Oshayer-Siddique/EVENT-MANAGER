package com.oshayer.event_manager.events.dto;

import lombok.*;
import java.math.BigDecimal;
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
    private ZonedDateTime eventStart;
    private ZonedDateTime eventEnd;

    private UUID venueId;
    private UUID seatLayoutId;

    private UUID eventManager;
    private UUID eventOperator1;
    private UUID eventOperator2;
    private UUID eventChecker1;
    private UUID eventChecker2;

    private Integer vipTickets;    private BigDecimal vipTicketPrice;
    private Integer platTickets;   private BigDecimal platTicketPrice;
    private Integer goldTickets;   private BigDecimal goldTicketPrice;
    private Integer silverTickets; private BigDecimal silverTicketPrice;

    private List<UUID> artistIds;
    private List<UUID> sponsorIds;
    private List<UUID> organizerIds;
}
