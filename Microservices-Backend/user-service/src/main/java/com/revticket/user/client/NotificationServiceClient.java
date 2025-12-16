package com.revticket.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", url = "http://localhost:8089", fallback = NotificationServiceClientFallback.class)
public interface NotificationServiceClient {
    
    @PostMapping("/api/notifications/admin/new-user")
    void sendAdminNewUser(@RequestBody Map<String, String> request);
}