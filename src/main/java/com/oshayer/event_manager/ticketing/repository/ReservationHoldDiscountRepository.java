package com.oshayer.event_manager.ticketing.repository;

import com.oshayer.event_manager.ticketing.entity.ReservationHoldDiscountEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ReservationHoldDiscountRepository extends JpaRepository<ReservationHoldDiscountEntity, UUID> {

    @Query("SELECT COUNT(DISTINCT hd.hold.id) FROM ReservationHoldDiscountEntity hd " +
            "WHERE hd.discount.id = :discountId AND hd.hold.status = :status " +
            "AND hd.hold.expiresAt > :now")
    long countActiveByDiscount(UUID discountId, ReservationHoldEntity.HoldStatus status, OffsetDateTime now);

    @Query("SELECT COUNT(DISTINCT hd.hold.id) FROM ReservationHoldDiscountEntity hd " +
            "WHERE hd.discount.id = :discountId AND hd.hold.status = :status " +
            "AND hd.hold.buyer.id = :buyerId AND hd.hold.expiresAt > :now")
    long countActiveByDiscountAndBuyer(UUID discountId, UUID buyerId, ReservationHoldEntity.HoldStatus status, OffsetDateTime now);
}
