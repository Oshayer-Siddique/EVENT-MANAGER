package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventSponsorLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventSponsorLinkRepository extends JpaRepository<EventSponsorLink, UUID> { }
