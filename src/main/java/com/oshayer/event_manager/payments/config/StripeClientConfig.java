package com.oshayer.event_manager.payments.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StripeClientConfig {

    private final StripeProperties properties;

    @PostConstruct
    public void configure() {
        if (!StringUtils.hasText(properties.getSecretKey())) {
            log.warn("Stripe secret key is not configured; payment APIs will fail until it is provided.");
            return;
        }
        Stripe.apiKey = properties.getSecretKey();
    }
}
