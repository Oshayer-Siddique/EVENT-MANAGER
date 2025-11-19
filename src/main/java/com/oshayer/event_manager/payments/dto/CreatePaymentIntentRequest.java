package com.oshayer.event_manager.payments.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePaymentIntentRequest {
    @NotNull
    private UUID holdId;

    private String currency;

    @Email
    @Size(max = 255)
    private String customerEmail;

    @Size(max = 255)
    private String description;
}
