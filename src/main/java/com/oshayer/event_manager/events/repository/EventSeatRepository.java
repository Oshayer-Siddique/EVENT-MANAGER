package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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

    @Modifying
    void deleteAllByEvent_Id(UUID eventId);
}
