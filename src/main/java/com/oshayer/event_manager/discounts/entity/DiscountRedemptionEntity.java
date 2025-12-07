package com.oshayer.event_manager.discounts.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "discount_redemptions",
        indexes = {
                @Index(name = "idx_discount_redemption_discount", columnList = "discount_id"),
                @Index(name = "idx_discount_redemption_buyer", columnList = "buyer_id")
        }
)
public class DiscountRedemptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid default uuid_generate_v4()")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    private DiscountEntity discount;

    @Column(name = "buyer_id")
    private UUID buyerId;

    @Column(name = "hold_id")
    private UUID holdId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private OffsetDateTime redeemedAt;
}
