package com.oshayer.event_manager.events.service;

import com.oshayer.event_manager.events.dto.CreateEventRequest;
import com.oshayer.event_manager.events.dto.EventResponse;

public interface EventService {
    EventResponse create(CreateEventRequest request);
}
