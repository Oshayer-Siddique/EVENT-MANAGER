package com.oshayer.event_manager.discounts.service;

import com.oshayer.event_manager.discounts.dto.*;
import com.oshayer.event_manager.discounts.entity.DiscountEntity;
import com.oshayer.event_manager.discounts.entity.DiscountEntity.DiscountValueType;
import com.oshayer.event_manager.discounts.dto.DiscountValueTypeDto;
import com.oshayer.event_manager.discounts.entity.DiscountRedemptionEntity;
import com.oshayer.event_manager.discounts.repository.DiscountRedemptionRepository;
import com.oshayer.event_manager.discounts.repository.DiscountRepository;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldDiscountEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;
import com.oshayer.event_manager.ticketing.repository.ReservationHoldDiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountRedemptionRepository redemptionRepository;
    private final ReservationHoldDiscountRepository holdDiscountRepository;

    @Override
    public DiscountResponse create(DiscountCreateRequest request) {
        DiscountEntity entity = new DiscountEntity();
        applyRequest(request, entity);
        entity = discountRepository.save(entity);
        return toResponse(entity);
    }

    @Override
    public DiscountResponse update(UUID id, DiscountUpdateRequest request) {
        DiscountEntity entity = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount not found"));
        applyRequest(request, entity);
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponse get(UUID id) {
        return discountRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Discount not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> list(UUID eventId) {
        return discountRepository.findAll().stream()
                .filter(discount -> eventId == null || discount.getEventId() == null || discount.getEventId().equals(eventId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DiscountValidationResponse preview(DiscountValidationRequest request) {
        DiscountCalculationRequest calcRequest = DiscountCalculationRequest.builder()
                .eventId(request.getEventId())
                .buyerId(request.getBuyerId())
                .discountCode(request.getDiscountCode())
                .includeAutomaticDiscounts(request.isIncludeAutomaticDiscounts())
                .items(request.getItems())
                .build();
        DiscountCalculationResult result = calculateForHold(calcRequest);
        return DiscountValidationResponse.builder()
                .subtotal(result.getSubtotal())
                .discountTotal(result.getDiscountTotal())
                .totalDue(result.getTotalDue())
                .appliedDiscounts(result.getAppliedDiscounts())
                .build();
    }

    @Override
    public DiscountCalculationResult calculateForHold(DiscountCalculationRequest request) {
        if (request.getBuyerId() == null && StringUtils.hasText(request.getDiscountCode())) {
            throw new IllegalStateException("You must be signed in to redeem discount codes.");
        }

        BigDecimal subtotal = calculateSubtotal(request.getItems());
        OffsetDateTime now = OffsetDateTime.now();
        List<DiscountEntity> ordered = resolveApplicableDiscounts(request, now);

        BigDecimal remaining = subtotal;
        List<DiscountValidationResponseItem> applied = new ArrayList<>();
        List<DiscountEntity> appliedEntities = new ArrayList<>();
        boolean allowStacking = true;

        for (DiscountEntity discount : ordered) {
            if (!allowStacking && !applied.isEmpty()) {
                break;
            }

            if (!discount.isActive() || !isWithinWindow(discount, now)) {
                continue;
            }
            if (!discount.isAllowGuestRedemption() && request.getBuyerId() == null && !discount.isAutoApply()) {
                continue;
            }
            if (!isEventMatch(discount, request.getEventId())) {
                continue;
            }

            enforceUsageLimits(discount, request.getBuyerId(), now);

            BigDecimal eligible = eligibleSubtotal(discount, request.getItems());
            if (eligible.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (!meetsMinimum(discount, eligible, subtotal)) {
                continue;
            }

            BigDecimal discountAmount = calculateAmount(discount, eligible);
            if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (discount.getMaxDiscountAmount() != null && discount.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = discountAmount.min(discount.getMaxDiscountAmount());
            }
            discountAmount = discountAmount.min(remaining);
            if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            remaining = remaining.subtract(discountAmount);
            applied.add(DiscountValidationResponseItem.builder()
                    .discountId(discount.getId())
                    .code(discount.getCode())
                    .name(discount.getName())
                    .amount(discountAmount.setScale(2, RoundingMode.HALF_UP))
                    .autoApplied(discount.isAutoApply())
                    .stackable(discount.isStackable())
                    .build());
            appliedEntities.add(discount);

            allowStacking = discount.isStackable();
        }

        BigDecimal discountTotal = applied.stream()
                .map(DiscountValidationResponseItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DiscountCalculationResult.builder()
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .discountTotal(discountTotal.setScale(2, RoundingMode.HALF_UP))
                .totalDue(subtotal.subtract(discountTotal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))
                .appliedDiscounts(applied)
                .appliedDiscountEntities(appliedEntities)
                .build();
    }

    @Override
    public void recordRedemptionForHold(ReservationHoldEntity hold) {
        if (hold.getAppliedDiscounts() == null || hold.getAppliedDiscounts().isEmpty()) {
            return;
        }
        for (ReservationHoldDiscountEntity applied : hold.getAppliedDiscounts()) {
            DiscountRedemptionEntity redemption = DiscountRedemptionEntity.builder()
                    .discount(applied.getDiscount())
                    .buyerId(hold.getBuyer() != null ? hold.getBuyer().getId() : null)
                    .holdId(hold.getId())
                    .amount(applied.getAmount())
                    .build();
            redemptionRepository.save(redemption);
        }
    }

    private void applyRequest(DiscountRequest request, DiscountEntity entity) {
        entity.setName(request.getName().trim());
        entity.setCode(request.getCode().trim().toUpperCase());
        DiscountValueTypeDto dtoType = request.getValueType();
        entity.setValueType(dtoType != null
                ? DiscountValueType.valueOf(dtoType.name())
                : entity.getValueType());
        entity.setValue(request.getValue());
        entity.setMaxDiscountAmount(request.getMaxDiscountAmount());
        entity.setMinimumOrderAmount(request.getMinimumOrderAmount());
        entity.setMaxRedemptions(request.getMaxRedemptions());
        entity.setMaxRedemptionsPerBuyer(request.getMaxRedemptionsPerBuyer());
        entity.setStartsAt(request.getStartsAt());
        entity.setEndsAt(request.getEndsAt());
        entity.setEventId(request.getEventId());
        entity.setTierCode(request.getTierCode());
        entity.setAutoApply(request.isAutoApply());
        entity.setStackable(request.isStackable());
        entity.setActive(request.isActive());
        entity.setAllowGuestRedemption(request.isAllowGuestRedemption());
        entity.setPriority(request.getPriority() != null ? request.getPriority() : entity.getPriority());
        entity.setNotes(request.getNotes());
    }

    private DiscountResponse toResponse(DiscountEntity entity) {
        return DiscountResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .valueType(entity.getValueType() != null
                        ? DiscountValueTypeDto.valueOf(entity.getValueType().name())
                        : null)
                .value(entity.getValue())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .minimumOrderAmount(entity.getMinimumOrderAmount())
                .maxRedemptions(entity.getMaxRedemptions())
                .maxRedemptionsPerBuyer(entity.getMaxRedemptionsPerBuyer())
                .startsAt(entity.getStartsAt())
                .endsAt(entity.getEndsAt())
                .eventId(entity.getEventId())
                .tierCode(entity.getTierCode())
                .autoApply(entity.isAutoApply())
                .stackable(entity.isStackable())
                .active(entity.isActive())
                .allowGuestRedemption(entity.isAllowGuestRedemption())
                .priority(entity.getPriority())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private BigDecimal calculateSubtotal(List<DiscountLineItem> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<DiscountEntity> resolveApplicableDiscounts(DiscountCalculationRequest request, OffsetDateTime now) {
        List<DiscountEntity> ordered = new ArrayList<>();

        DiscountEntity manual = null;
        if (StringUtils.hasText(request.getDiscountCode())) {
            manual = discountRepository.findByCodeIgnoreCase(request.getDiscountCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid discount code."));
            ordered.add(manual);
        }
        if (request.isIncludeAutomaticDiscounts()) {
            ordered.addAll(discountRepository.findActiveAutoApplicable(request.getEventId(), now));
        }

        LinkedHashMap<UUID, DiscountEntity> deduped = new LinkedHashMap<>();
        for (DiscountEntity discount : ordered) {
            deduped.putIfAbsent(discount.getId(), discount);
        }

        return deduped.values().stream()
                .filter(DiscountEntity::isActive)
                .sorted(Comparator
                        .comparing(DiscountEntity::isAutoApply)
                        .thenComparingInt(DiscountEntity::getPriority))
                .collect(Collectors.toList());
    }

    private boolean isWithinWindow(DiscountEntity discount, OffsetDateTime now) {
        if (discount.getStartsAt() != null && discount.getStartsAt().isAfter(now)) {
            return false;
        }
        return discount.getEndsAt() == null || !discount.getEndsAt().isBefore(now);
    }

    private boolean isEventMatch(DiscountEntity discount, UUID eventId) {
        return discount.getEventId() == null || discount.getEventId().equals(eventId);
    }

    private BigDecimal eligibleSubtotal(DiscountEntity discount, List<DiscountLineItem> items) {
        if (!StringUtils.hasText(discount.getTierCode())) {
            return calculateSubtotal(items);
        }
        String tier = discount.getTierCode().trim().toLowerCase();
        return items.stream()
                .filter(item -> tier.equalsIgnoreCase(item.getTierCode()))
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean meetsMinimum(DiscountEntity discount, BigDecimal eligibleSubtotal, BigDecimal orderSubtotal) {
        if (discount.getMinimumOrderAmount() != null && discount.getMinimumOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
            return orderSubtotal.compareTo(discount.getMinimumOrderAmount()) >= 0;
        }
        return eligibleSubtotal.compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal calculateAmount(DiscountEntity discount, BigDecimal eligibleSubtotal) {
        if (discount.getValueType() == DiscountValueType.AMOUNT) {
            return discount.getValue();
        }
        BigDecimal percent = discount.getValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return eligibleSubtotal.multiply(percent);
    }

    private void enforceUsageLimits(DiscountEntity discount, UUID buyerId, OffsetDateTime now) {
        if (discount.getMaxRedemptions() != null) {
            long total = redemptionRepository.countByDiscount_Id(discount.getId())
                    + holdDiscountRepository.countActiveByDiscount(discount.getId(), ReservationHoldEntity.HoldStatus.ACTIVE, now);
            if (total >= discount.getMaxRedemptions()) {
                throw new IllegalStateException("Discount " + discount.getCode() + " has reached its usage limit.");
            }
        }
        if (buyerId != null && discount.getMaxRedemptionsPerBuyer() != null) {
            long perBuyer = redemptionRepository.countByDiscount_IdAndBuyerId(discount.getId(), buyerId)
                    + holdDiscountRepository.countActiveByDiscountAndBuyer(discount.getId(), buyerId, ReservationHoldEntity.HoldStatus.ACTIVE, now);
            if (perBuyer >= discount.getMaxRedemptionsPerBuyer()) {
                throw new IllegalStateException("You have reached the redemption limit for discount " + discount.getCode() + ".");
            }
        }
    }
}
