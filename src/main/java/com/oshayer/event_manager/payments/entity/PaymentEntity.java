package com.oshayer.event_manager.payments.entity;

import com.oshayer.event_manager.events.entity.EventEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_intent", columnNames = "stripe_payment_intent_id")
        })
public class PaymentEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hold_id", nullable = false)
    private ReservationHoldEntity hold;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "stripe_payment_intent_id", length = 64)
    private String stripePaymentIntentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.REQUIRES_PAYMENT_METHOD;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
