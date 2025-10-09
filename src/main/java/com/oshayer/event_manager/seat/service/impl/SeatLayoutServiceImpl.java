package com.oshayer.event_manager.seat.service.impl;

import com.oshayer.event_manager.seat.dto.SeatLayoutDTO;
import com.oshayer.event_manager.seat.entity.SeatLayout;
import com.oshayer.event_manager.seat.repository.SeatLayoutRepository;
import com.oshayer.event_manager.seat.service.SeatLayoutService;
import com.oshayer.event_manager.venues.entity.EventVenue;
import com.oshayer.event_manager.venues.repository.EventVenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatLayoutServiceImpl implements SeatLayoutService {

    private final SeatLayoutRepository seatLayoutRepository;
    private final EventVenueRepository venueRepository;

    @Override
    public SeatLayoutDTO createSeatLayout(UUID venueId, SeatLayoutDTO dto) {
        EventVenue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found: " + venueId));

        if (seatLayoutRepository.existsByVenue_IdAndLayoutName(venueId, dto.getLayoutName())) {
            throw new IllegalStateException("Layout name already exists for this venue");
        }

        SeatLayout layout = SeatLayout.builder()
                .typeCode(dto.getTypeCode())
                .typeName(dto.getTypeName())
                .venue(venue)
                .layoutName(dto.getLayoutName())
                .totalRows(dto.getTotalRows())
                .totalCols(dto.getTotalCols())
                .totalTables(dto.getTotalTables())
                .chairsPerTable(dto.getChairsPerTable())
                .standingCapacity(dto.getStandingCapacity())
                .totalCapacity(dto.getTotalCapacity())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE)
                .build();

        SeatLayout saved = seatLayoutRepository.save(layout);
        return toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SeatLayoutDTO getSeatLayout(UUID id) {
        return seatLayoutRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatLayoutDTO> getSeatLayoutsByVenue(UUID venueId) {
        return seatLayoutRepository.findByVenue_Id(venueId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public SeatLayoutDTO updateSeatLayout(UUID id, SeatLayoutDTO dto) {
        SeatLayout layout = seatLayoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + id));

        // if name is changed, enforce per-venue uniqueness
        if (!Objects.equals(layout.getLayoutName(), dto.getLayoutName()) &&
                seatLayoutRepository.existsByVenue_IdAndLayoutName(layout.getVenue().getId(), dto.getLayoutName())) {
            throw new IllegalStateException("Layout name already exists for this venue");
        }

        layout.setTypeCode(dto.getTypeCode());
        layout.setTypeName(dto.getTypeName());
        layout.setLayoutName(dto.getLayoutName());
        layout.setTotalRows(dto.getTotalRows());
        layout.setTotalCols(dto.getTotalCols());
        layout.setTotalTables(dto.getTotalTables());
        layout.setChairsPerTable(dto.getChairsPerTable());
        layout.setStandingCapacity(dto.getStandingCapacity());
        layout.setTotalCapacity(dto.getTotalCapacity());
        layout.setIsActive(dto.getIsActive());

        return toDTO(seatLayoutRepository.save(layout));
    }

    @Override
    public void deleteSeatLayout(UUID id) {
        seatLayoutRepository.deleteById(id);
    }

    private SeatLayoutDTO toDTO(SeatLayout layout) {
        return SeatLayoutDTO.builder()
                .id(layout.getId())
                .typeCode(layout.getTypeCode())
                .typeName(layout.getTypeName())
                .venueId(layout.getVenue().getId())
                .layoutName(layout.getLayoutName())
                .totalRows(layout.getTotalRows())
                .totalCols(layout.getTotalCols())
                .totalTables(layout.getTotalTables())
                .chairsPerTable(layout.getChairsPerTable())
                .standingCapacity(layout.getStandingCapacity())
                .totalCapacity(layout.getTotalCapacity())
                .isActive(layout.getIsActive())
                .build();
    }
}
