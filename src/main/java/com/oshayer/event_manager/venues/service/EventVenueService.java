package com.oshayer.event_manager.venues.service;

import com.oshayer.event_manager.venues.dto.EventVenueDTO;

import java.util.List;
import java.util.UUID;

public interface EventVenueService {
    EventVenueDTO createVenue(EventVenueDTO dto);
    EventVenueDTO getVenue(UUID id);
    List<EventVenueDTO> getAllVenues();
    EventVenueDTO updateVenue(UUID id, EventVenueDTO dto);
    void deleteVenue(UUID id);
}
