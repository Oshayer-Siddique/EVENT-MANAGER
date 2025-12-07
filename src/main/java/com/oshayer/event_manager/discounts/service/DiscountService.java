package com.oshayer.event_manager.discounts.service;

import com.oshayer.event_manager.discounts.dto.*;
import com.oshayer.event_manager.discounts.entity.DiscountEntity;
import com.oshayer.event_manager.ticketing.entity.ReservationHoldEntity;

import java.util.List;
import java.util.UUID;

public interface DiscountService {
    DiscountResponse create(DiscountCreateRequest request);
    DiscountResponse update(UUID id, DiscountUpdateRequest request);
    DiscountResponse get(UUID id);
    List<DiscountResponse> list(UUID eventId);
    DiscountValidationResponse preview(DiscountValidationRequest request);

    DiscountCalculationResult calculateForHold(DiscountCalculationRequest request);

    void recordRedemptionForHold(ReservationHoldEntity hold);
}
