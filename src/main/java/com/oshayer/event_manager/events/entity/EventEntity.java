package com.oshayer.event_manager.events.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime; // If your project uses OffsetDateTime elsewhere, switch for consistency.
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "event_description", columnDefinition = "text")
    private String eventDescription;

    @Column(name = "privacy_policy", columnDefinition = "text")
    private String privacyPolicy;

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

    @OneToMany(mappedBy = "eventId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventTicketTier> ticketTiers = new ArrayList<>();

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

    @ElementCollection
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();
}
