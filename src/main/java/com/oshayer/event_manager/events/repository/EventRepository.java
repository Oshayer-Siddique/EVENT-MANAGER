package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    Optional<EventEntity> findByEventCode(String eventCode);
    boolean existsByEventCode(String eventCode);

    @Query("""
        select count(e) from EventEntity e
        where e.eventStart <= :now and e.eventEnd >= :now
    """)
    long countLive(ZonedDateTime now);

    @Query("""
        select count(e) from EventEntity e
        where e.eventStart > :now
    """)
    long countUpcoming(ZonedDateTime now);

    @Query("""
        select count(e) from EventEntity e
        where e.eventEnd < :now
    """)
    long countCompleted(ZonedDateTime now);

    @Query("""
        select count(e) from EventEntity e
        where e.venueId = :venueId
          and e.eventStart <= :now
          and e.eventEnd >= :now
    """)
    long countLiveByVenue(UUID venueId, ZonedDateTime now);

    @Query("""
        select count(e) from EventEntity e
        where e.venueId = :venueId
          and e.eventStart > :now
    """)
    long countUpcomingByVenue(UUID venueId, ZonedDateTime now);

    @Query("""
        select count(e) from EventEntity e
        where e.venueId = :venueId
          and e.eventEnd < :now
    """)
    long countCompletedByVenue(UUID venueId, ZonedDateTime now);
}
