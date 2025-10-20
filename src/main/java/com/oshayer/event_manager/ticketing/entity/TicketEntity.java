package com.oshayer.event_manager.ticketing.entity;

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
                @UniqueConstraint(name = "uk_ticket_qr_code", columnNames = "qr_code"),
                // one seat per event; NULL allowed for GA
                @UniqueConstraint(name = "uk_ticket_event_seat", columnNames = {"event_id","seat_label"})
        },
        indexes = {
                @Index(name = "idx_ticket_event", columnList = "event_id"),
                @Index(name = "idx_ticket_buyer", columnList = "buyer_id"),
                @Index(name = "idx_ticket_status", columnList = "status"),
                @Index(name = "idx_ticket_tier", columnList = "tier_code")
        }
)
public class TicketEntity {

    @Id @GeneratedValue private UUID id;

    // links (UUIDs to your existing tables)
    @Column(name = "event_id", nullable = false) private UUID eventId;    // EventEntity.id
    @Column(name = "buyer_id", nullable = false) private UUID buyerId;    // UserEntity.id
    @Column(name = "seat_layout_id") private UUID seatLayoutId;           // SeatLayout.id (nullable)

    // tier
    @Column(name = "tier_code", nullable = false, length = 20)
    private String tierCode;  // VIP / PLAT / GOLD / SILVER

    // optional seat mapping
    @Column(name = "seat_label")
    private String seatLabel; // e.g., "A-10"; null for GA

    // money snapshot
    @Column(name = "currency", length = 10, nullable = false)
    private String currency = "BDT";
    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

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
    @Column(name = "checker_id") private UUID checkerId;   // UserEntity.id
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

    public void setRefunedAt(OffsetDateTime now) {
    }

    public enum TicketStatus { PENDING, ISSUED, USED, CANCELED, EXPIRED, REFUNDED }
}
