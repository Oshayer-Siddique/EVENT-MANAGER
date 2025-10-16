package com.oshayer.event_manager.seat.repository;

import com.oshayer.event_manager.seat.entity.SeatLayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatLayoutRepository extends JpaRepository<SeatLayout, UUID> {
    List<SeatLayout> findByVenue_Id(UUID venueId);                  // keep
    boolean existsByVenue_IdAndLayoutName(UUID venueId, String layoutName); // keep

    // NEW: used to enforce layout belongs to the venue during event creation
    Optional<SeatLayout> findByIdAndVenue_Id(UUID id, UUID venueId);
}
