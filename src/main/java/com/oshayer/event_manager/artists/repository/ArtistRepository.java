package com.oshayer.event_manager.artists.repository;

import com.oshayer.event_manager.artists.entity.ArtistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArtistRepository extends JpaRepository<ArtistEntity, UUID> {
    Optional<ArtistEntity> findByName(String name);
}
