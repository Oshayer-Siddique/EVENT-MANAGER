package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventOrganizerLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventOrganizerLinkRepository extends JpaRepository<EventOrganizerLink, UUID> { }
