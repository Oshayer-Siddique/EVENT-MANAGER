package com.oshayer.event_manager.events.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(
        name = "event_artists",
        uniqueConstraints = @UniqueConstraint(name="uk_event_artist", columnNames={"event_id","artist_id"})
)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EventArtistLink {
    @Id @GeneratedValue private UUID id;

    @Column(name="event_id", nullable=false) private UUID eventId;
    @Column(name="artist_id", nullable=false) private UUID artistId;
}
