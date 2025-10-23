package com.oshayer.event_manager.ticketing.entity;

import com.oshayer.event_manager.events.entity.EventSeatEntity;
import com.oshayer.event_manager.users.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ticket_qr_code", columnNames = "qr_code")
                // The OneToOne relationship to event_seat_id now guarantees uniqueness per seat per event.
        },
        indexes = {
                @Index(name = "idx_ticket_buyer", columnList = "buyer_id"),
                @Index(name = "idx_ticket_status", columnList = "status")
        }
)
public class TicketEntity {

    @Id @GeneratedValue private UUID id;

    // This is the core link. A ticket IS the sale of a specific EventSeat.
    // The unique=true constraint on JoinColumn enforces the OneToOne relationship at the DB level.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_seat_id", nullable = false, unique = true)
    private EventSeatEntity eventSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private UserEntity buyer;

    // lifecycle
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status = TicketStatus.PENDING;

    @Column(name = "reserved_until") private OffsetDateTime reservedUntil;
    @Column(name = "issued_at") private OffsetDateTime issuedAt;
    @Column(name = "checked_in_at") private OffsetDateTime checkedInAt;

    // verification
    @Column(name = "qr_code", nullable = false, unique = true, length = 256)
    private String qrCode;
    @Column(name = "verification_code", length = 32)
    private String verificationCode;

    // holder / staff
    @Column(name = "holder_name") private String holderName;
    @Column(name = "holder_email") private String holderEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checker_id")
    private UserEntity checker;

    @Column(name = "gate") private String gate;

    // refunds
    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;
    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;

    // audit / concurrency
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    @Version @Column(nullable = false) private Long version = 0L;

    // Note: The buggy `setRefunedAt` method has been removed.

    public enum TicketStatus { PENDING, ISSUED, USED, CANCELED, EXPIRED, REFUNDED }
}
