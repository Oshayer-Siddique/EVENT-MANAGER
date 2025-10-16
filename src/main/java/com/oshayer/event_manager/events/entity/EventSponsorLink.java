package com.oshayer.event_manager.events.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(
        name = "event_sponsors",
        uniqueConstraints = @UniqueConstraint(name="uk_event_sponsor", columnNames={"event_id","sponsor_id"})
)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EventSponsorLink {
    @Id @GeneratedValue private UUID id;

    @Column(name="event_id", nullable=false) private UUID eventId;
    @Column(name="sponsor_id", nullable=false) private UUID sponsorId;
}
