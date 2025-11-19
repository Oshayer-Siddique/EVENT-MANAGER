package com.oshayer.event_manager.payments.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    /** Secret API key used for server-to-Stripe calls. */
    private String secretKey;

    /** Webhook signing secret for verifying Stripe callbacks. */
    private String webhookSecret;

    /** Default currency for new payment intents (e.g., usd). */
    private String currency = "usd";
}
