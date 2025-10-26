package com.oshayer.event_manager.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "event_ticket_tiers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_ticket_tier", columnNames = {"event_id", "tier_code"})
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EventTicketTier {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid default uuid_generate_v4()")
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "tier_code", nullable = false, length = 20)
    private String tierCode;

    @Column(name = "tier_name", nullable = false, length = 50)
    private String tierName;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "cost", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "visible", nullable = false)
    @Builder.Default
    private Boolean visible = Boolean.TRUE;

    @Builder.Default
    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity = 0;

    @Builder.Default
    @Column(name = "used_quantity", nullable = false)
    private Integer usedQuantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "edited_at", nullable = false)
    private ZonedDateTime editedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
