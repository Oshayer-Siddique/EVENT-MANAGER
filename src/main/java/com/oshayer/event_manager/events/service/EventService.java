package com.oshayer.event_manager.events.service;

import com.oshayer.event_manager.events.dto.EventRequest;
import com.oshayer.event_manager.events.dto.EventResponse;

import java.util.List;
import java.util.UUID;

public interface EventService {
    EventResponse createEvent(EventRequest request);
    EventResponse updateEvent(UUID id, EventRequest request);
    EventResponse getEventById(UUID id);
    List<EventResponse> getAllEvents();
    void deleteEvent(UUID id);
}
