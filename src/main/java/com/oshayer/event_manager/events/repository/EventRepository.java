package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    Optional<EventEntity> findByEventCode(String eventCode);
    boolean existsByEventCode(String eventCode);
}
