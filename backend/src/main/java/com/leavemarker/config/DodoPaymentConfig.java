package com.leavemarker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "dodo.payment")
@Getter
@Setter
public class DodoPaymentConfig {
    private String apiKey;
    private String apiSecret;
    private String webhookSecret;
    private String baseUrl;
    private String returnUrl;
    private String cancelUrl;
}
