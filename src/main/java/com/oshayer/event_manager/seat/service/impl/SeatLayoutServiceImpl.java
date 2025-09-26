package com.oshayer.event_manager.seat.service.impl;

import com.oshayer.event_manager.seat.dto.SeatLayoutDTO;
import com.oshayer.event_manager.seat.entity.SeatLayout;
import com.oshayer.event_manager.seat.repository.SeatLayoutRepository;
import com.oshayer.event_manager.seat.service.SeatLayoutService;
import com.oshayer.event_manager.venues.repository.EventVenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatLayoutServiceImpl implements SeatLayoutService {

    private final SeatLayoutRepository seatLayoutRepository;
    private final EventVenueRepository venueRepository;

    @Override
    public SeatLayoutDTO createSeatLayout(SeatLayoutDTO dto) {
        SeatLayout layout = SeatLayout.builder()
                .typeCode(dto.getTypeCode())
                .typeName(dto.getTypeName())
                .venue(venueRepository.findById(dto.getVenueId())
                        .orElseThrow(() -> new RuntimeException("Venue not found")))
                .layoutName(dto.getLayoutName())
                .totalRows(dto.getTotalRows())
                .totalCols(dto.getTotalCols())
                .totalTables(dto.getTotalTables())
                .chairsPerTable(dto.getChairsPerTable())
                .standingCapacity(dto.getStandingCapacity())
                .totalCapacity(dto.getTotalCapacity())
                .isActive(dto.getIsActive())
                .build();
        layout = seatLayoutRepository.save(layout);
        dto.setId(layout.getId());
        return dto;
    }

    @Override
    public SeatLayoutDTO getSeatLayout(UUID id) {
        return seatLayoutRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Seat layout not found"));
    }

    @Override
    public List<SeatLayoutDTO> getSeatLayoutsByVenue(UUID venueId) {
        return seatLayoutRepository.findByVenueId(venueId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public SeatLayoutDTO updateSeatLayout(UUID id, SeatLayoutDTO dto) {
        SeatLayout layout = seatLayoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seat layout not found"));
        layout.setLayoutName(dto.getLayoutName());
        layout.setTotalRows(dto.getTotalRows());
        layout.setTotalCols(dto.getTotalCols());
        layout.setTotalTables(dto.getTotalTables());
        layout.setChairsPerTable(dto.getChairsPerTable());
        layout.setStandingCapacity(dto.getStandingCapacity());
        layout.setTotalCapacity(dto.getTotalCapacity());
        layout.setIsActive(dto.getIsActive());
        seatLayoutRepository.save(layout);
        return toDTO(layout);
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
