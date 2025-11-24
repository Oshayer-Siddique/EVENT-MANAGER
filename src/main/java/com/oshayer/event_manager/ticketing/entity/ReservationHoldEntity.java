package com.oshayer.event_manager.ticketing.entity;

import com.oshayer.event_manager.events.entity.EventEntity;
import com.oshayer.event_manager.events.entity.EventSeatEntity;
import com.oshayer.event_manager.users.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id") // nullable for guest
    private UserEntity buyer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HoldStatus status = HoldStatus.ACTIVE;

    // A hold now consists of a list of specific event seats
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "reservation_hold_seats",
            joinColumns = @JoinColumn(name = "hold_id"),
            inverseJoinColumns = @JoinColumn(name = "event_seat_id")
    )
    private List<EventSeatEntity> heldSeats;

    // Store JSON as regular text so Postgres returns it via getString rather than CLOB/OID
    @Column(name = "items_json", nullable = false, columnDefinition = "text")
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
