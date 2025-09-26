package com.oshayer.event_manager.seat.entity;

import com.oshayer.event_manager.venues.entity.EventVenue;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "seat_layout")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayout {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String typeCode;

    @Column(nullable = false)
    private String typeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private EventVenue venue;

    @Column(nullable = false, unique = true)
    private String layoutName;

    private Integer totalRows;
    private Integer totalCols;
    private Integer totalTables;
    private Integer chairsPerTable;
    private Integer standingCapacity;

    @Column(nullable = false)
    private Integer totalCapacity;

    @Column(nullable = false)
    private Boolean isActive = true;

    private String createdBy;
    private java.time.OffsetDateTime createdAt = java.time.OffsetDateTime.now();
    private String editedBy;
    private java.time.OffsetDateTime editedAt;
    private String dataDigest;
}
