package com.oshayer.event_manager.payments.controller;

import com.oshayer.event_manager.payments.dto.CreatePaymentIntentRequest;
import com.oshayer.event_manager.payments.dto.CreatePaymentIntentResponse;
import com.oshayer.event_manager.payments.service.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intents")
    public ResponseEntity<CreatePaymentIntentResponse> createIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        try {
            return ResponseEntity.ok(paymentService.createPaymentIntent(request));
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (StripeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe API error: " + ex.getMessage(), ex);
        }
    }
}
