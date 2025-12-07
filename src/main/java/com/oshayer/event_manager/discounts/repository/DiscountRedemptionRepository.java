package com.oshayer.event_manager.discounts.repository;

import com.oshayer.event_manager.discounts.entity.DiscountRedemptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface DiscountRedemptionRepository extends JpaRepository<DiscountRedemptionEntity, UUID> {

    long countByDiscount_Id(UUID discountId);

    long countByDiscount_IdAndBuyerId(UUID discountId, UUID buyerId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM DiscountRedemptionEntity r WHERE r.discount.id = :discountId")
    java.math.BigDecimal sumDiscountedAmount(UUID discountId);
}
