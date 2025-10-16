package com.oshayer.event_manager.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime; // If your project uses OffsetDateTime elsewhere, switch for consistency.
import java.util.UUID;

@Entity
@Table(
        name = "events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_events_event_code", columnNames = "event_code")
        },
        indexes = {
                @Index(name = "idx_events_start", columnList = "event_start"),
                @Index(name = "idx_events_venue", columnList = "venue_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid default uuid_generate_v4()")
    private UUID id;

    // Identity
    @Column(name = "type_code", nullable = false)
    private String typeCode;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "event_code", nullable = false)
    private String eventCode;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    // Schedule
    @Column(name = "event_start", nullable = false)
    private ZonedDateTime eventStart;

    @Column(name = "event_end", nullable = false)
    private ZonedDateTime eventEnd;

    // Location (kept as UUID to match your current design)
    @Column(name = "venue_id", nullable = false)
    private UUID venueId;

    // Optional: enforce capacity against a specific layout
    @Column(name = "seat_layout_id")
    private UUID seatLayoutId;

    // Staffing (UUIDs of users)
    @Column(name = "event_manager", nullable = false)
    private UUID eventManager;

    @Column(name = "event_operator1", nullable = false)
    private UUID eventOperator1;

    @Column(name = "event_operator2")
    private UUID eventOperator2;

    @Column(name = "event_checker1", nullable = false)
    private UUID eventChecker1;

    @Column(name = "event_checker2")
    private UUID eventChecker2;

    // Ticket tiers (use BigDecimal for currency)
    @Column(name = "vip_tickets")
    private Integer vipTickets;

    @Column(name = "vip_ticket_price", precision = 12, scale = 2)
    private BigDecimal vipTicketPrice;

    @Builder.Default
    @Column(name = "vip_tickets_sold", nullable = false)
    private Integer vipTicketsSold = 0;

    @Builder.Default
    @Column(name = "vip_tickets_used", nullable = false)
    private Integer vipTicketsUsed = 0;

    @Column(name = "plat_tickets")
    private Integer platTickets;

    @Column(name = "plat_ticket_price", precision = 12, scale = 2)
    private BigDecimal platTicketPrice;

    @Builder.Default
    @Column(name = "plat_tickets_sold", nullable = false)
    private Integer platTicketsSold = 0;

    @Builder.Default
    @Column(name = "plat_tickets_used", nullable = false)
    private Integer platTicketsUsed = 0;

    @Column(name = "gold_tickets")
    private Integer goldTickets;

    @Column(name = "gold_ticket_price", precision = 12, scale = 2)
    private BigDecimal goldTicketPrice;

    @Builder.Default
    @Column(name = "gold_tickets_sold", nullable = false)
    private Integer goldTicketsSold = 0;

    @Builder.Default
    @Column(name = "gold_tickets_used", nullable = false)
    private Integer goldTicketsUsed = 0;

    @Column(name = "silver_tickets")
    private Integer silverTickets;

    @Column(name = "silver_ticket_price", precision = 12, scale = 2)
    private BigDecimal silverTicketPrice;

    @Builder.Default
    @Column(name = "silver_tickets_sold", nullable = false)
    private Integer silverTicketsSold = 0;

    @Builder.Default
    @Column(name = "silver_tickets_used", nullable = false)
    private Integer silverTicketsUsed = 0;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "edited_at", nullable = false)
    private ZonedDateTime editedAt;

    @Column(name = "data_digest")
    private String dataDigest;

    // Concurrency safety
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
