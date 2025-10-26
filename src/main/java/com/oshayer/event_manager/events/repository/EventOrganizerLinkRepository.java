package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventOrganizerLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventOrganizerLinkRepository extends JpaRepository<EventOrganizerLink, UUID> {

    List<EventOrganizerLink> findByEventId(UUID eventId);

    boolean existsByEventIdAndOrgId(UUID eventId, UUID orgId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from EventOrganizerLink l where l.eventId = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);
}
