package com.oshayer.event_manager.events.service.impl;

import com.oshayer.event_manager.events.dto.EventRequest;
import com.oshayer.event_manager.events.dto.EventResponse;
import com.oshayer.event_manager.events.entity.EventEntity;
import com.oshayer.event_manager.events.repository.EventRepository;
import com.oshayer.event_manager.events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;

    @Override

    public EventResponse createEvent(EventRequest request) {
        EventEntity entity = modelMapper.map(request, EventEntity.class);
        entity.setId(null); // 🚀 force INSERT
        eventRepository.save(entity);
        return modelMapper.map(entity, EventResponse.class);
    }


    @Override
    public EventResponse updateEvent(UUID id, EventRequest request) {
        EventEntity entity = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Manually update fields to avoid overwriting ID
        entity.setTypeCode(request.getTypeCode());
        entity.setTypeName(request.getTypeName());
        entity.setEventCode(request.getEventCode());
        entity.setEventName(request.getEventName());
        entity.setEventStart(request.getEventStart());
        entity.setEventEnd(request.getEventEnd());
        entity.setVenueId(request.getVenueId());

        eventRepository.save(entity);
        return modelMapper.map(entity, EventResponse.class);
    }


    @Override
    public EventResponse getEventById(UUID id) {
        EventEntity entity = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return modelMapper.map(entity, EventResponse.class);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(event -> modelMapper.map(event, EventResponse.class))
                .toList();
    }

    @Override
    public void deleteEvent(UUID id) {
        eventRepository.deleteById(id);
    }
}
