package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventSponsorLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventSponsorLinkRepository extends JpaRepository<EventSponsorLink, UUID> {

    List<EventSponsorLink> findByEventId(UUID eventId);

    boolean existsByEventIdAndSponsorId(UUID eventId, UUID sponsorId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from EventSponsorLink l where l.eventId = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);
}
