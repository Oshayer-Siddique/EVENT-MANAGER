package com.oshayer.event_manager.payments.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.oshayer.event_manager.payments.dto.CreatePaymentIntentRequest;
import com.oshayer.event_manager.payments.dto.CreatePaymentIntentResponse;

public interface PaymentService {
    CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) throws StripeException;

    void handleWebhook(String payload, String signatureHeader) throws SignatureVerificationException, StripeException;
}
