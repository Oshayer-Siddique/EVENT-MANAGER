package com.oshayer.event_manager.venues.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventVenueDTO {
    private UUID id;
    private String typeCode;
    private String typeName;
    private String venueCode;
    private String venueName;
    private String address;
    private String email;
    private String phone;
    private Integer totalEvents;
    private Integer liveEvents;
    private Integer eventsUpcoming;
}
