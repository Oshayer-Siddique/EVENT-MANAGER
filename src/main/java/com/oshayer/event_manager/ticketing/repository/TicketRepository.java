package com.oshayer.event_manager.ticketing.repository;

import com.oshayer.event_manager.ticketing.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {

    Optional<TicketEntity> findByQrCode(String qrCode);

    /**
     * Finds all tickets for a given event by joining through the EventSeat entity.
     * Spring Data JPA automatically creates the query from the method name.
     */
    List<TicketEntity> findByEventSeat_Event_Id(UUID eventId);

    @Modifying
    void deleteAllByEventSeat_Event_Id(UUID eventId);

    /**
     * Finds all tickets for a given buyer by their ID.
     */
    List<TicketEntity> findByBuyer_Id(UUID buyerId);

}
