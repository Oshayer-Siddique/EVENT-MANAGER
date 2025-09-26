package com.oshayer.event_manager.seat.repository;

import com.oshayer.event_manager.seat.entity.SeatLayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatLayoutRepository extends JpaRepository<SeatLayout, UUID> {
    List<SeatLayout> findByVenueId(UUID venueId);
}
