package com.oshayer.event_manager.events.service;

import com.oshayer.event_manager.events.dto.CreateEventRequest;
import com.oshayer.event_manager.events.dto.EventResponse;
import com.oshayer.event_manager.events.dto.EventSeatMapResponse;
import com.oshayer.event_manager.events.dto.EventSeatResponse;
import com.oshayer.event_manager.events.dto.EventTicketDetailsResponse;
import com.oshayer.event_manager.events.dto.SeatAssignmentUpdateRequest;
import com.oshayer.event_manager.events.dto.SeatInventorySyncRequest;
import com.oshayer.event_manager.events.dto.UpdateEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EventService {
    EventResponse create(CreateEventRequest request);
    EventResponse get(UUID id);
    EventTicketDetailsResponse getTicketDetails(UUID id);
    List<EventSeatResponse> listSeats(UUID id);
    List<EventSeatResponse> syncSeatInventory(UUID id, SeatInventorySyncRequest request);
    EventSeatMapResponse getSeatMap(UUID id);
    List<EventSeatResponse> updateSeatAssignments(UUID id, SeatAssignmentUpdateRequest request);
    Page<EventResponse> list(Pageable pageable); // simple listing; can add filters later
    EventResponse update(UUID id, UpdateEventRequest request);
    void delete(UUID id);
}
