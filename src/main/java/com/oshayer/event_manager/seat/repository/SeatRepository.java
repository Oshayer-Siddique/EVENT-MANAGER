package com.oshayer.event_manager.seat.repository;

import com.oshayer.event_manager.seat.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {
    List<SeatEntity> findBySeatLayout_IdOrderByRowAscNumberAsc(UUID layoutId);

    boolean existsBySeatLayout_IdAndLabel(UUID layoutId, String label);
}
