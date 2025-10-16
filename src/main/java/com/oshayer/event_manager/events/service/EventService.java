package com.oshayer.event_manager.events.service;

import com.oshayer.event_manager.events.dto.CreateEventRequest;
import com.oshayer.event_manager.events.dto.EventResponse;
import com.oshayer.event_manager.events.dto.UpdateEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    EventResponse create(CreateEventRequest request);
    EventResponse get(UUID id);
    Page<EventResponse> list(Pageable pageable); // simple listing; can add filters later
    EventResponse update(UUID id, UpdateEventRequest request);
    void delete(UUID id);
}
