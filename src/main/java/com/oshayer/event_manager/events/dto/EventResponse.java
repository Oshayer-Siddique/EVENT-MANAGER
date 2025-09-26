package com.oshayer.event_manager.events.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class EventResponse {
    private UUID id;
    private String typeCode;
    private String typeName;
    private String eventCode;
    private String eventName;
    private ZonedDateTime eventStart;
    private ZonedDateTime eventEnd;
    private UUID venueId;
}
