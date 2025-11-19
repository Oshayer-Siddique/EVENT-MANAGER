package com.oshayer.event_manager.payments.repository;

import com.oshayer.event_manager.payments.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByStripePaymentIntentId(String intentId);
}
