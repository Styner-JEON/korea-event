package com.bridge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryTemplateConfig {

    @Value("${retry.max-attempts}")
    private int maxAttempts;

    @Value("${retry.initial-interval}")
    private long initialInterval;

    @Value("${retry.multiplier}")
    private double multiplier;

    @Value("${retry.max-interval}")
    private long maxInterval;

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(maxAttempts)
                .exponentialBackoff(initialInterval, multiplier, maxInterval)
                .retryOn(Throwable.class)
                .traversingCauses()
                .build();
    }

}
