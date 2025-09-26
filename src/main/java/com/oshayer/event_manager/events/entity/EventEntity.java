package com.oshayer.event_manager.events.entity;

import com.oshayer.event_manager.organizations.entity.OrganizationEntity;
import com.oshayer.event_manager.users.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid default uuid_generate_v4()")
    private UUID id;

    private String typeCode;
    private String typeName;

    @Column(unique = true, nullable = false)
    private String eventCode;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private ZonedDateTime eventStart;

    @Column(nullable = false)
    private ZonedDateTime eventEnd;

    // Venue stored as UUID
    private UUID venueId;

    // Operators as UUID (or String usernames if you prefer)
    private UUID eventManager;
    private UUID eventOperator1;
    private UUID eventOperator2;
    private UUID eventChecker1;
    private UUID eventChecker2;

    // Tickets
    private Integer vipTickets;
    private Double vipTicketPrice;
    private Integer vipTicketsSold;
    private Integer vipTicketsUsed;

    private Integer platTickets;
    private Double platTicketPrice;
    private Integer platTicketsSold;
    private Integer platTicketsUsed;

    private Integer goldTickets;
    private Double goldTicketPrice;
    private Integer goldTicketsSold;
    private Integer goldTicketsUsed;

    private Integer silverTickets;
    private Double silverTicketPrice;
    private Integer silverTicketsSold;
    private Integer silverTicketsUsed;

    // Audit
    private UUID createdBy;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    private UUID editedBy;

    @UpdateTimestamp
    private ZonedDateTime editedAt;

    private String dataDigest;
}
