package com.oshayer.event_manager.venues.service.impl;

import com.oshayer.event_manager.venues.dto.EventVenueDTO;
import com.oshayer.event_manager.venues.entity.EventVenue;
import com.oshayer.event_manager.venues.repository.EventVenueRepository;
import com.oshayer.event_manager.venues.service.EventVenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventVenueServiceImpl implements EventVenueService {

    private final EventVenueRepository venueRepository;

    @Override
    public EventVenueDTO createVenue(EventVenueDTO dto) {
        EventVenue venue = EventVenue.builder()
                .typeCode(dto.getTypeCode())
                .typeName(dto.getTypeName())
                .venueCode(dto.getVenueCode())
                .venueName(dto.getVenueName())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .totalEvents(dto.getTotalEvents())
                .liveEvents(dto.getLiveEvents())
                .eventsUpcoming(dto.getEventsUpcoming())
                .build();
        venue = venueRepository.save(venue);
        dto.setId(venue.getId());
        return dto;
    }

    @Override
    public EventVenueDTO getVenue(UUID id) {
        return venueRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
    }

    @Override
    public List<EventVenueDTO> getAllVenues() {
        return venueRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public EventVenueDTO updateVenue(UUID id, EventVenueDTO dto) {
        EventVenue venue = venueRepository.findById(id).orElseThrow(() -> new RuntimeException("Venue not found"));
        venue.setVenueName(dto.getVenueName());
        venue.setVenueCode(dto.getVenueCode());
        venue.setAddress(dto.getAddress());
        venue.setEmail(dto.getEmail());
        venue.setPhone(dto.getPhone());
        venueRepository.save(venue);
        return toDTO(venue);
    }

    @Override
    public void deleteVenue(UUID id) {
        venueRepository.deleteById(id);
    }

    private EventVenueDTO toDTO(EventVenue venue) {
        return EventVenueDTO.builder()
                .id(venue.getId())
                .typeCode(venue.getTypeCode())
                .typeName(venue.getTypeName())
                .venueCode(venue.getVenueCode())
                .venueName(venue.getVenueName())
                .address(venue.getAddress())
                .email(venue.getEmail())
                .phone(venue.getPhone())
                .totalEvents(venue.getTotalEvents())
                .liveEvents(venue.getLiveEvents())
                .eventsUpcoming(venue.getEventsUpcoming())
                .build();
    }
}
