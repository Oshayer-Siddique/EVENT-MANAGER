package com.oshayer.event_manager.ticketing.repository;

import com.oshayer.event_manager.ticketing.entity.TicketEntity;
import com.oshayer.event_manager.ticketing.entity.TicketEntity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface TicketRepository extends JpaRepository<TicketEntity, java.util.UUID> {

    Optional<TicketEntity> findByQrCode(String qrCode);

    boolean existsByEventIdAndSeatLabelAndStatusIn(
            java.util.UUID eventId, String seatLabel, Collection<TicketStatus> statuses);

    long countByEventIdAndTierCodeAndStatusIn(
            java.util.UUID eventId, String tierCode, Collection<TicketStatus> statuses);

    List<TicketEntity> findByEventId(java.util.UUID eventId);

    @Query("select t from TicketEntity t where t.eventId = :eventId and t.status in :statuses")
    List<TicketEntity> findAllByEventIdAndStatuses(java.util.UUID eventId, Collection<TicketStatus> statuses);
}
