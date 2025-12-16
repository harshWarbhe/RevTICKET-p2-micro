package com.revticket.user.client;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationServiceClientFallback implements NotificationServiceClient {
    
    @Override
    public void sendAdminNewUser(Map<String, String> request) {
        System.out.println("Notification service unavailable - admin new user notification not sent");
    }
}