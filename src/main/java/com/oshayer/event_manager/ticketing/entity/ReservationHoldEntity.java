package com.oshayer.event_manager.ticketing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "reservation_holds",
        indexes = {
                @Index(name = "idx_hold_event", columnList = "event_id"),
                @Index(name = "idx_hold_buyer", columnList = "buyer_id"),
                @Index(name = "idx_hold_status", columnList = "status"),
                @Index(name = "idx_hold_expires", columnList = "expires_at")
        }
)
public class ReservationHoldEntity {

    @Id @GeneratedValue private UUID id;

    @Column(name = "event_id", nullable = false) private UUID eventId; // EventEntity.id
    @Column(name = "buyer_id") private UUID buyerId;                   // UserEntity.id (nullable for guest)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HoldStatus status = HoldStatus.ACTIVE;

    /**
     * jsonb content example:
     * [
     *   {"tierCode":"VIP","qty":2},
     *   {"tierCode":"GOLD","seatLayoutId":"<uuid>","seats":["A-10","A-11"]}
     * ]
     */
    @Column(name = "items_json", columnDefinition = "jsonb", nullable = false)
    private String itemsJson;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "finalized_payment_id")
    private UUID finalizedPaymentId; // optional PaymentEntity.id

    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum HoldStatus { ACTIVE, CONVERTED, RELEASED, EXPIRED }
}
