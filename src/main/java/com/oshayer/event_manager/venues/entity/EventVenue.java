package com.oshayer.event_manager.venues.entity;

import com.oshayer.event_manager.seat.entity.SeatLayout;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "event_venues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventVenue {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String typeCode = "100";

    @Column(nullable = false)
    private String typeName = "Theatre";

    @Column(nullable = false, unique = true)
    private String venueCode;

    @Column(nullable = false, unique = true)
    private String venueName;

    @Column(nullable = false)
    private String address;

    private String email;
    private String phone;

    @Column(nullable = false)
    private Integer totalEvents = 0;

    @Column(nullable = false)
    private Integer liveEvents = 0;

    @Column(nullable = false)
    private Integer eventsUpcoming = 0;

    private String createdBy;

    @CreationTimestamp   // ✅ Hibernate will auto-fill
    @Column(nullable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;

    private String editedBy;

    @UpdateTimestamp    // ✅ Hibernate will auto-update
    private java.time.OffsetDateTime editedAt;

    private String dataDigest;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatLayout> seatLayouts;
}
