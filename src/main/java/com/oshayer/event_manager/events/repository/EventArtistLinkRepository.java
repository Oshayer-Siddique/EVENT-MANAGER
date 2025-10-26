package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventArtistLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventArtistLinkRepository extends JpaRepository<EventArtistLink, UUID> {

    List<EventArtistLink> findByEventId(UUID eventId);

    boolean existsByEventIdAndArtistId(UUID eventId, UUID artistId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from EventArtistLink l where l.eventId = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);
}
