package com.oshayer.event_manager.venues.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.oshayer.event_manager.seat.entity.SeatLayout;

import java.util.List;
import java.util.UUID;

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

    private String maxCapacity;
    private String mapAddress;
    private String socialMediaLink;
    private String websiteLink;

    @Column(nullable = false)
    private String address;

    private String email;
    private String phone;

    @Column(nullable = false)
    private Integer totalEvents = 0;

    @Column(nullable = false)
    private Integer liveEvents = 0;

    @Column(name = "events_upcoming", nullable = false)
    private Integer eventsUpcoming = 0;

    private String createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;

    private String editedBy;

    @UpdateTimestamp
    private java.time.OffsetDateTime editedAt;

    private String dataDigest;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatLayout> seatLayouts;
}
