package com.oshayer.event_manager.events.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(
        name = "event_organizers",
        uniqueConstraints = @UniqueConstraint(name="uk_event_organizer", columnNames={"event_id","org_id"})
)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EventOrganizerLink {
    @Id @GeneratedValue private UUID id;

    @Column(name="event_id", nullable=false) private UUID eventId;
    @Column(name="org_id",   nullable=false) private UUID orgId;
}
