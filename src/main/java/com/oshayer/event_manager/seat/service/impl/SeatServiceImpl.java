package com.oshayer.event_manager.seat.service.impl;

import com.oshayer.event_manager.seat.dto.SeatDTO;
import com.oshayer.event_manager.seat.entity.SeatEntity;
import com.oshayer.event_manager.seat.entity.SeatLayout;
import com.oshayer.event_manager.seat.repository.SeatLayoutRepository;
import com.oshayer.event_manager.seat.repository.SeatRepository;
import com.oshayer.event_manager.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final SeatLayoutRepository seatLayoutRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SeatDTO> getSeats(UUID layoutId) {
        ensureLayoutExists(layoutId);
        return seatRepository.findBySeatLayout_IdOrderByRowAscNumberAsc(layoutId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public SeatDTO createSeat(UUID layoutId, SeatDTO dto) {
        SeatLayout layout = ensureLayoutExists(layoutId);

        SeatEntity seat = new SeatEntity();
        seat.setSeatLayout(layout);

        applySeatAttributes(layoutId, seat, dto);

        return toDTO(seatRepository.save(seat));
    }

    @Override
    public SeatDTO updateSeat(UUID layoutId, UUID seatId, SeatDTO dto) {
        SeatEntity seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));

        if (!seat.getSeatLayout().getId().equals(layoutId)) {
            throw new IllegalArgumentException("Seat does not belong to the given layout");
        }

        applySeatAttributes(layoutId, seat, dto);

        return toDTO(seatRepository.save(seat));
    }

    @Override
    public void deleteSeat(UUID layoutId, UUID seatId) {
        SeatEntity seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));

        if (!seat.getSeatLayout().getId().equals(layoutId)) {
            throw new IllegalArgumentException("Seat does not belong to the given layout");
        }

        seatRepository.delete(seat);
    }

    private SeatLayout ensureLayoutExists(UUID layoutId) {
        return seatLayoutRepository.findById(layoutId)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + layoutId));
    }

    private void applySeatAttributes(UUID layoutId, SeatEntity seat, SeatDTO dto) {
        String row = dto.getRow() != null ? dto.getRow().trim() : null;
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("Row is required");
        }

        Integer number = dto.getNumber();
        if (number == null || number < 1) {
            throw new IllegalArgumentException("Number must be greater than zero");
        }

        String normalizedRow = row.toUpperCase(Locale.ROOT);
        String label = dto.getLabel();
        if (label == null || label.isBlank()) {
            label = normalizedRow + "-" + number;
        }

        boolean labelChanged = seat.getId() == null || !label.equals(seat.getLabel())
                || !seat.getSeatLayout().getId().equals(layoutId);
        if (labelChanged && seatRepository.existsBySeatLayout_IdAndLabel(layoutId, label)) {
            throw new IllegalStateException("Seat label already exists in this layout: " + label);
        }

        seat.setRow(normalizedRow);
        seat.setNumber(number);
        seat.setLabel(label);
        seat.setType(dto.getType());
    }

    private SeatDTO toDTO(SeatEntity seat) {
        return SeatDTO.builder()
                .id(seat.getId())
                .layoutId(seat.getSeatLayout().getId())
                .row(seat.getRow())
                .number(seat.getNumber())
                .label(seat.getLabel())
                .type(seat.getType())
                .build();
    }
}

