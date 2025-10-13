package com.oshayer.event_manager.sponsors.repository;

import com.oshayer.event_manager.sponsors.entity.SponsorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SponsorRepository extends JpaRepository<SponsorEntity, UUID> {
    Optional<SponsorEntity> findByName(String name);
}

