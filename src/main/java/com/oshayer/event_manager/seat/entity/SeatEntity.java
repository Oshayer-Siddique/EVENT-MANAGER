package com.oshayer.event_manager.seat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                // A seat label like "A-10" must be unique within its layout
                @UniqueConstraint(name = "uk_seat_layout_label", columnNames = {"seat_layout_id", "label"})
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_layout_id", nullable = false)
    private SeatLayout seatLayout;

    @Column(name = "seat_row", nullable = false)
    private String row;

    @Column(name = "seat_number", nullable = false)
    private Integer number;

    @Column(nullable = false)
    private String label; // e.g., "A-10"

    private String type; // e.g., "WHEELCHAIR", "STANDARD"
}
