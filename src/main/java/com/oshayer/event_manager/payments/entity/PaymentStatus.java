package com.oshayer.event_manager.payments.entity;

public enum PaymentStatus {
    REQUIRES_PAYMENT_METHOD,
    REQUIRES_CONFIRMATION,
    REQUIRES_ACTION,
    PROCESSING,
    SUCCEEDED,
    CANCELED,
    FAILED
}
