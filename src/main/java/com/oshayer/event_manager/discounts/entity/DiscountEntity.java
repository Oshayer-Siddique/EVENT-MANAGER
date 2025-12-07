package com.oshayer.event_manager.discounts.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
        name = "discounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_discount_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_discount_event", columnList = "event_id"),
                @Index(name = "idx_discount_active", columnList = "active")
        }
)
public class DiscountEntity {

    public enum DiscountValueType {
        AMOUNT, PERCENTAGE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid default uuid_generate_v4()")
    private UUID id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    private DiscountValueType valueType;

    @Column(name = "value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", precision = 12, scale = 2)
    private BigDecimal minimumOrderAmount;

    @Column(name = "max_redemptions")
    private Integer maxRedemptions;

    @Column(name = "max_redemptions_per_buyer")
    private Integer maxRedemptionsPerBuyer;

    @Column(name = "starts_at")
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "tier_code", length = 32)
    private String tierCode;

    @Column(name = "auto_apply", nullable = false)
    @Builder.Default
    private boolean autoApply = false;

    @Column(name = "stackable", nullable = false)
    @Builder.Default
    private boolean stackable = false;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "allow_guest_redemption", nullable = false)
    @Builder.Default
    private boolean allowGuestRedemption = false;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private int priority = 0;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
