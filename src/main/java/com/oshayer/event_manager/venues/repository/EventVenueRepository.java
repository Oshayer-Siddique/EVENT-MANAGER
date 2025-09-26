package com.oshayer.event_manager.venues.repository;

import com.oshayer.event_manager.venues.entity.EventVenue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventVenueRepository extends JpaRepository<EventVenue, UUID> {
    Optional<EventVenue> findByVenueCode(String venueCode);
}
