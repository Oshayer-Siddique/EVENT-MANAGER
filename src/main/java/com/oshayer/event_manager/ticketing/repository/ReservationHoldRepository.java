package com.oshayer.event_manager.ticketing.repository;

import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationHoldRepository extends JpaRepository<ReservationHoldEntity, UUID> {

    List<ReservationHoldEntity> findByEvent_IdAndStatus(UUID eventId, HoldStatus status);

    @Query("""
        select h from ReservationHoldEntity h
        where h.event.id = :eventId and h.status = 'ACTIVE' and h.expiresAt > :now
    """)
    List<ReservationHoldEntity> findActiveNotExpired(UUID eventId, OffsetDateTime now);
}

