package com.oshayer.event_manager.seat.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oshayer.event_manager.seat.dto.BanquetLayoutDTO;
import com.oshayer.event_manager.seat.dto.HybridLayoutDTO;
import com.oshayer.event_manager.seat.dto.HybridLayoutDTO.ElementDefinition;
import com.oshayer.event_manager.seat.dto.HybridLayoutDTO.SeatDefinition;
import com.oshayer.event_manager.seat.dto.HybridLayoutDTO.SectionDefinition;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
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
                .layoutConfiguration(serializeConfiguration(dto.getConfiguration()))
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

    @Override
    @Transactional(readOnly = true)
    public HybridLayoutDTO getHybridLayout(UUID layoutId) {
        SeatLayout layout = seatLayoutRepository.findById(layoutId)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + layoutId));
        return sanitizeHybridLayout(decodeHybridLayout(layout.getLayoutConfiguration()));
    }

    @Override
    public HybridLayoutDTO updateHybridLayout(UUID layoutId, HybridLayoutDTO layoutDTO) {
        SeatLayout layout = seatLayoutRepository.findById(layoutId)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + layoutId));

        HybridLayoutDTO sanitized = sanitizeHybridLayout(layoutDTO);
        layout.setLayoutConfiguration(encodeHybridLayout(sanitized));

        int seatCount = sanitized.getSeats() != null ? sanitized.getSeats().size() : 0;
        layout.setTotalCapacity(Math.max(seatCount, 0));
        layout.setTotalRows(null);
        layout.setTotalCols(null);
        layout.setTotalTables(null);
        layout.setChairsPerTable(null);

        syncHybridSeats(layout, sanitized);

        seatLayoutRepository.save(layout);
        return sanitized;
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
        if (dto.getConfiguration() != null) {
            layout.setLayoutConfiguration(serializeConfiguration(dto.getConfiguration()));
        }

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
                .configuration(parseConfiguration(layout.getLayoutConfiguration()))
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

    private HybridLayoutDTO decodeHybridLayout(String configurationJson) {
        if (configurationJson == null || configurationJson.isBlank()) {
            return HybridLayoutDTO.builder().build();
        }
        try {
            return objectMapper.readValue(configurationJson, HybridLayoutDTO.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to parse hybrid layout configuration", ex);
        }
    }

    private String encodeHybridLayout(HybridLayoutDTO layout) {
        if (layout == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(layout);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize hybrid layout", ex);
        }
    }

    private JsonNode parseConfiguration(String configurationJson) {
        if (configurationJson == null || configurationJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(configurationJson);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to parse layout configuration", ex);
        }
    }

    private String serializeConfiguration(JsonNode configuration) {
        if (configuration == null || configuration.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(configuration);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize layout configuration", ex);
        }
    }

    private void syncBanquetSeats(SeatLayout layout, BanquetLayoutDTO layoutDTO) {
        UUID layoutId = layout.getId();
        seatRepository.deleteAllBySeatLayout_Id(layoutId);

        if (layoutDTO.getTables() == null || layoutDTO.getTables().isEmpty()) {
            return;
        }

        Set<String> usedRowLabels = new HashSet<>();
        List<SeatEntity> seats = new ArrayList<>();
        int tableIndex = 0;
        for (BanquetLayoutDTO.BanquetTableDTO table : layoutDTO.getTables()) {
            String sanitizedLabel = sanitizeRowLabel(table.getLabel(), tableIndex);
            String rowLabel = ensureUniqueRowLabel(sanitizedLabel, usedRowLabels);
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

    private HybridLayoutDTO sanitizeHybridLayout(HybridLayoutDTO layoutDTO) {
        HybridLayoutDTO source = layoutDTO != null ? layoutDTO : HybridLayoutDTO.builder().build();

        HybridLayoutDTO.CanvasSpec canvas = source.getCanvas();
        if (canvas == null) {
            canvas = HybridLayoutDTO.CanvasSpec.builder()
                    .width(1200d)
                    .height(800d)
                    .gridSize(20d)
                    .zoom(1d)
                    .build();
        }

        List<SectionDefinition> sections = new ArrayList<>();
        if (source.getSections() != null) {
            for (SectionDefinition section : source.getSections()) {
                if (section == null) {
                    continue;
                }
                sections.add(SectionDefinition.builder()
                        .id(section.getId() != null ? section.getId() : UUID.randomUUID())
                        .label(section.getLabel() != null ? section.getLabel().trim() : null)
                        .shape(section.getShape() != null ? section.getShape().trim() : null)
                        .x(section.getX())
                        .y(section.getY())
                        .width(section.getWidth())
                        .height(section.getHeight())
                        .radius(section.getRadius())
                        .rotation(section.getRotation())
                        .color(section.getColor())
                        .build());
            }
        }

        List<ElementDefinition> elements = new ArrayList<>();
        if (source.getElements() != null) {
            for (ElementDefinition element : source.getElements()) {
                if (element == null) {
                    continue;
                }
                elements.add(ElementDefinition.builder()
                        .id(element.getId() != null ? element.getId() : UUID.randomUUID())
                        .type(element.getType() != null ? element.getType().trim() : null)
                        .label(element.getLabel() != null ? element.getLabel().trim() : null)
                        .x(element.getX())
                        .y(element.getY())
                        .width(element.getWidth())
                        .height(element.getHeight())
                        .radius(element.getRadius())
                        .rotation(element.getRotation())
                        .color(element.getColor())
                        .build());
            }
        }

        List<SeatDefinition> seats = new ArrayList<>();
        if (source.getSeats() != null) {
            for (SeatDefinition seat : source.getSeats()) {
                if (seat == null) {
                    continue;
                }
                seats.add(SeatDefinition.builder()
                        .id(seat.getId() != null ? seat.getId() : UUID.randomUUID())
                        .sectionId(seat.getSectionId())
                        .label(seat.getLabel() != null ? seat.getLabel().trim() : null)
                        .rowLabel(seat.getRowLabel() != null ? seat.getRowLabel().trim() : null)
                        .number(seat.getNumber())
                        .tierCode(seat.getTierCode())
                        .type(seat.getType())
                        .x(seat.getX())
                        .y(seat.getY())
                        .rotation(seat.getRotation())
                        .radius(seat.getRadius())
                        .build());
            }
        }

        return HybridLayoutDTO.builder()
                .canvas(canvas)
                .sections(sections)
                .elements(elements)
                .seats(seats)
                .build();
    }

    private void syncHybridSeats(SeatLayout layout, HybridLayoutDTO layoutDTO) {
        UUID layoutId = layout.getId();
        seatRepository.deleteAllBySeatLayout_Id(layoutId);

        List<SeatDefinition> seats = layoutDTO.getSeats();
        if (seats == null || seats.isEmpty()) {
            return;
        }

        Map<UUID, SectionDefinition> sectionsById = layoutDTO.getSections() == null
                ? Map.of()
                : layoutDTO.getSections().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SectionDefinition::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        Map<String, Integer> rowCounters = new HashMap<>();
        List<SeatEntity> entities = new ArrayList<>();

        for (SeatDefinition definition : seats) {
            if (definition == null) {
                continue;
            }

            String resolvedRow = definition.getRowLabel();
            if ((resolvedRow == null || resolvedRow.isBlank()) && definition.getSectionId() != null) {
                SectionDefinition section = sectionsById.get(definition.getSectionId());
                if (section != null) {
                    resolvedRow = section.getLabel();
                }
            }
            String sanitizedRow = sanitizeHybridRowLabel(resolvedRow);

            int number;
            if (definition.getNumber() != null && definition.getNumber() > 0) {
                number = definition.getNumber();
                rowCounters.merge(sanitizedRow, number, Math::max);
            } else {
                number = rowCounters.merge(sanitizedRow, 1, Integer::sum);
            }

            String label = definition.getLabel();
            if (label == null || label.isBlank()) {
                label = sanitizedRow + "-" + number;
            } else {
                label = label.trim();
            }

            SeatEntity entity = new SeatEntity();
            entity.setSeatLayout(layout);
            entity.setRow(sanitizedRow);
            entity.setNumber(number);
            entity.setLabel(label);
            entity.setType(definition.getType() != null && !definition.getType().isBlank()
                    ? definition.getType().trim().toUpperCase(Locale.ROOT)
                    : "HYBRID");
            entities.add(entity);
        }

        if (!entities.isEmpty()) {
            seatRepository.saveAll(entities);
        }
    }

    private String sanitizeHybridRowLabel(String label) {
        String base = (label == null || label.isBlank()) ? "ZONE" : label.trim();
        String sanitized = base.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (sanitized.isBlank()) {
            sanitized = "ZONE";
        }
        return sanitized;
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

    private String ensureUniqueRowLabel(String sanitizedLabel, Set<String> usedRowLabels) {
        if (sanitizedLabel == null || sanitizedLabel.isBlank()) {
            sanitizedLabel = "TABLE";
        }
        String candidate = sanitizedLabel;
        int suffix = 2;
        while (usedRowLabels.contains(candidate)) {
            candidate = sanitizedLabel + suffix;
            suffix++;
        }
        usedRowLabels.add(candidate);
        return candidate;
    }
}
