package com.revticket.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
    
    @GetMapping("/api/admin/payments/stats")
    Map<String, Object> getPaymentStats();
}
