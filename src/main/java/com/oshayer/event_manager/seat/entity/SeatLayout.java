package com.oshayer.event_manager.seat.entity;

import com.oshayer.event_manager.venues.entity.EventVenue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "seat_layout",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_seat_layout_venue_name",
                columnNames = {"venue_id", "layout_name"}
        ),
        indexes = @Index(name = "idx_seat_layout_venue", columnList = "venue_id")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayout {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false) private String typeCode;
    @Column(nullable = false) private String typeName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private EventVenue venue;

    @Column(name = "layout_name", nullable = false)
    private String layoutName;          // unique per venue (see @Table)

    private Integer totalRows;
    private Integer totalCols;
    private Integer totalTables;
    private Integer chairsPerTable;
    private Integer standingCapacity;

    @Column(nullable = false) private Integer totalCapacity;
    @Column(nullable = false) private Boolean isActive = true;

    private String createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private String editedBy;

    @UpdateTimestamp
    private OffsetDateTime editedAt;

    private String dataDigest;
}
