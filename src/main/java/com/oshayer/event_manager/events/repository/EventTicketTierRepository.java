package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventTicketTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventTicketTierRepository extends JpaRepository<EventTicketTier, UUID> {
    List<EventTicketTier> findByEventId(UUID eventId);
    void deleteAllByEventId(UUID eventId);
}
