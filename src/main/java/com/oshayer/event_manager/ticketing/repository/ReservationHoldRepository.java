package com.oshayer.event_manager.ticketing.repository;

import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.*;

public interface ReservationHoldRepository extends JpaRepository<ReservationHoldEntity, java.util.UUID> {

    List<ReservationHoldEntity> findByEventIdAndStatus(java.util.UUID eventId, HoldStatus status);

    @Query("""
        select h from ReservationHoldEntity h
        where h.eventId = :eventId and h.status = 'ACTIVE' and h.expiresAt > :now
    """)
    List<ReservationHoldEntity> findActiveNotExpired(java.util.UUID eventId, OffsetDateTime now);
}
