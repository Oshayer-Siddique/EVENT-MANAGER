package com.oshayer.event_manager.discounts.repository;

import com.oshayer.event_manager.discounts.entity.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiscountRepository extends JpaRepository<DiscountEntity, UUID> {

    Optional<DiscountEntity> findByCodeIgnoreCase(String code);

    @Query("SELECT d FROM DiscountEntity d WHERE d.autoApply = true AND d.active = true " +
            "AND (d.eventId IS NULL OR d.eventId = :eventId) " +
            "AND (d.startsAt IS NULL OR d.startsAt <= :now) " +
            "AND (d.endsAt IS NULL OR d.endsAt >= :now)")
    List<DiscountEntity> findActiveAutoApplicable(UUID eventId, OffsetDateTime now);
}
