package com.oshayer.event_manager.events.repository;

import com.oshayer.event_manager.events.entity.EventArtistLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventArtistLinkRepository extends JpaRepository<EventArtistLink, UUID> { }
