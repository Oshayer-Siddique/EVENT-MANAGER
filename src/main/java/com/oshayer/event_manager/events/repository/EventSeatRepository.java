package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventSeatEntity;
import com.oshayer.event_manager.events.entity.EventSeatEntity.EventSeatStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface EventSeatRepository extends JpaRepository<EventSeatEntity, UUID> {

    /**
     * Finds a specific EventSeat based on the event and the seat.
     * This is a key method for the ticket creation process.
     */
    @Query("""
        select es from EventSeatEntity es
        join es.seat s
        where es.event.id = :eventId and s.id = :seatId
    """)
    Optional<EventSeatEntity> findByEventIdAndSeatId(UUID eventId, UUID seatId);

    @Query("""
        select es from EventSeatEntity es
        join fetch es.seat s
        where es.event.id = :eventId
    """)
    List<EventSeatEntity> findByEventId(UUID eventId);

    long countByEvent_IdAndTierCode(UUID eventId, String tierCode);

    @Query("""
        select count(es) from EventSeatEntity es
        where es.event.id = :eventId
          and es.tierCode = :tierCode
          and es.status in :statuses
    """)
    long countByEventAndTierAndStatuses(UUID eventId, String tierCode, Collection<EventSeatStatus> statuses);

    @Query("""
        select es from EventSeatEntity es
        join fetch es.seat s
        where es.event.id = :eventId
          and es.tierCode = :tierCode
          and es.status = :status
        order by es.createdAt asc
    """)
    List<EventSeatEntity> findAvailableSeats(UUID eventId, String tierCode, EventSeatStatus status, Pageable pageable);

    @Modifying
    void deleteAllByEvent_Id(UUID eventId);
}
