package com.oshayer.event_manager.payments.dto;

import com.oshayer.event_manager.payments.entity.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CreatePaymentIntentResponse {
    UUID paymentId;
    String paymentIntentId;
    String clientSecret;
    Long amountCents;
    String currency;
    PaymentStatus status;
}
