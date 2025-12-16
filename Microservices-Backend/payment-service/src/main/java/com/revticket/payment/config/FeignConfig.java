package com.revticket.payment.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    // Removed RequestInterceptor - headers are explicitly set in Feign client
    // method calls
}
