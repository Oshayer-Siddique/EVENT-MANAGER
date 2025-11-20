package com.oshayer.event_manager.seat.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oshayer.event_manager.seat.dto.BanquetLayoutDTO;
import com.oshayer.event_manager.seat.dto.SeatLayoutDTO;
import com.oshayer.event_manager.seat.entity.SeatEntity;
import com.oshayer.event_manager.seat.entity.SeatLayout;
import com.oshayer.event_manager.seat.repository.SeatLayoutRepository;
import com.oshayer.event_manager.seat.repository.SeatRepository;
import com.oshayer.event_manager.seat.service.SeatLayoutService;
import com.oshayer.event_manager.venues.entity.EventVenue;
import com.oshayer.event_manager.venues.repository.EventVenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatLayoutServiceImpl implements SeatLayoutService {

    private final SeatLayoutRepository seatLayoutRepository;
    private final EventVenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final ObjectMapper objectMapper;

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
        return applyUpdates(layout, dto);
    }

    @Override
    public SeatLayoutDTO updateSeatLayout(UUID venueId, UUID layoutId, SeatLayoutDTO dto) {
        SeatLayout layout = seatLayoutRepository.findByIdAndVenue_Id(layoutId, venueId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seat layout not found for venue %s with id %s".formatted(venueId, layoutId)));

        return applyUpdates(layout, dto);
    }

    @Override
    public void deleteSeatLayout(UUID id) {
        seatLayoutRepository.deleteById(id);
    }

    @Override
    public void deleteSeatLayout(UUID venueId, UUID layoutId) {
        SeatLayout layout = seatLayoutRepository.findByIdAndVenue_Id(layoutId, venueId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seat layout not found for venue %s with id %s".formatted(venueId, layoutId)));
        seatLayoutRepository.delete(layout);
    }

    @Override
    @Transactional(readOnly = true)
    public BanquetLayoutDTO getBanquetLayout(UUID layoutId) {
        SeatLayout layout = seatLayoutRepository.findById(layoutId)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + layoutId));
        return decodeBanquetLayout(layout.getDataDigest());
    }

    @Override
    public BanquetLayoutDTO updateBanquetLayout(UUID layoutId, BanquetLayoutDTO layoutDTO) {
        SeatLayout layout = seatLayoutRepository.findById(layoutId)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + layoutId));

        layout.setDataDigest(encodeBanquetLayout(layoutDTO));

        int totalTables = layoutDTO.getTables() == null ? 0 : layoutDTO.getTables().size();
        layout.setTotalTables(totalTables);
        int totalChairs = layoutDTO.getTables() == null ? 0 : layoutDTO.getTables().stream()
                .mapToInt(table -> {
                    if (table.getChairs() != null && !table.getChairs().isEmpty()) {
                        return table.getChairs().size();
                    }
                    return table.getChairCount() != null ? table.getChairCount() : 0;
                })
                .sum();
        layout.setTotalCapacity(Math.max(totalChairs, 0));
        layout.setChairsPerTable(totalTables > 0 ? Math.max(totalChairs / totalTables, 0) : 0);
        layout.setTotalRows(totalTables);
        layout.setTotalCols(null);

        syncBanquetSeats(layout, layoutDTO);

        seatLayoutRepository.save(layout);
        return layoutDTO;
    }

    private SeatLayoutDTO applyUpdates(SeatLayout layout, SeatLayoutDTO dto) {
        UUID venueId = layout.getVenue().getId();

        if (!Objects.equals(layout.getLayoutName(), dto.getLayoutName()) &&
                seatLayoutRepository.existsByVenue_IdAndLayoutName(venueId, dto.getLayoutName())) {
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

    private BanquetLayoutDTO decodeBanquetLayout(String dataDigest) {
        if (dataDigest == null || dataDigest.isBlank()) {
            return BanquetLayoutDTO.builder().build();
        }
        try {
            return objectMapper.readValue(dataDigest, BanquetLayoutDTO.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse banquet layout data", ex);
        }
    }

    private String encodeBanquetLayout(BanquetLayoutDTO layout) {
        try {
            return objectMapper.writeValueAsString(layout);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize banquet layout", ex);
        }
    }

    private void syncBanquetSeats(SeatLayout layout, BanquetLayoutDTO layoutDTO) {
        UUID layoutId = layout.getId();
        seatRepository.deleteAllBySeatLayout_Id(layoutId);

        if (layoutDTO.getTables() == null || layoutDTO.getTables().isEmpty()) {
            return;
        }

        List<SeatEntity> seats = new ArrayList<>();
        int tableIndex = 0;
        for (BanquetLayoutDTO.BanquetTableDTO table : layoutDTO.getTables()) {
            String rowLabel = sanitizeRowLabel(table.getLabel(), tableIndex);
            List<BanquetLayoutDTO.BanquetChairDTO> chairs = normalizeChairs(table);
            int number = 1;
            for (BanquetLayoutDTO.BanquetChairDTO ignored : chairs) {
                SeatEntity seat = new SeatEntity();
                seat.setSeatLayout(layout);
                seat.setRow(rowLabel);
                seat.setNumber(number);
                seat.setLabel(rowLabel + "-" + number);
                seat.setType("BANQUET");
                seats.add(seat);
                number++;
            }
            tableIndex++;
        }

        if (!seats.isEmpty()) {
            seatRepository.saveAll(seats);
        }
    }

    private List<BanquetLayoutDTO.BanquetChairDTO> normalizeChairs(BanquetLayoutDTO.BanquetTableDTO table) {
        if (table.getChairs() != null && !table.getChairs().isEmpty()) {
            return table.getChairs();
        }
        int chairCount = table.getChairCount() != null ? table.getChairCount() : 0;
        if (chairCount <= 0) {
            chairCount = 1;
        }
        List<BanquetLayoutDTO.BanquetChairDTO> generated = new ArrayList<>();
        for (int i = 0; i < chairCount; i++) {
            generated.add(BanquetLayoutDTO.BanquetChairDTO.builder()
                    .id(UUID.randomUUID())
                    .label("Chair " + (i + 1))
                    .angle((360d / chairCount) * i)
                    .offsetX(0d)
                    .offsetY(0d)
                    .build());
        }
        return generated;
    }

    private String sanitizeRowLabel(String label, int index) {
        String base = (label == null || label.isBlank()) ? "TABLE" + (index + 1) : label;
        return base.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }
}
