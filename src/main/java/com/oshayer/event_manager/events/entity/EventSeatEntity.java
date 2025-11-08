package com.oshayer.event_manager.events.entity;

import com.oshayer.event_manager.seat.entity.SeatEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "event_seats",
        // This constraint is the core of the design: a seat can only appear once per event.
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_seat", columnNames = {"event_id", "seat_id"})
        },
        indexes = {
                @Index(name = "idx_event_seat_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSeatEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatEntity seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventSeatStatus status = EventSeatStatus.AVAILABLE;

    @Column(name = "tier_code", nullable = false, length = 20)
    private String tierCode; // e.g., VIP, GOLD. Set when generating the inventory.

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum EventSeatStatus {
        AVAILABLE,
        RESERVED, // Temporarily held (e.g., in a user's cart)
        SOLD,
        BLOCKED // Admin pre-reserved / blocked from sale
    }
}
