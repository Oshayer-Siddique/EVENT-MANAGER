package com.oshayer.event_manager.ticketing.entity;

import com.oshayer.event_manager.discounts.entity.DiscountEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservation_hold_discounts",
        indexes = {
                @Index(name = "idx_hold_discount_discount", columnList = "discount_id"),
                @Index(name = "idx_hold_discount_hold", columnList = "hold_id")
        })
public class ReservationHoldDiscountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid default uuid_generate_v4()")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hold_id", nullable = false)
    private ReservationHoldEntity hold;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    private DiscountEntity discount;

    @Column(name = "discount_code", nullable = false, length = 64)
    private String discountCode;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "auto_applied", nullable = false)
    private boolean autoApplied;

    @Column(name = "stack_rank")
    private Integer stackRank;
}
