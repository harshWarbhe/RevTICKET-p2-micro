package com.revticket.booking.client;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationServiceClientFallback implements NotificationServiceClient {
    
    @Override
    public void sendBookingConfirmation(Map<String, Object> request) {
        System.out.println("Notification service unavailable - booking confirmation not sent");
    }
    
    @Override
    public void sendCancellationRequest(Map<String, Object> request) {
        System.out.println("Notification service unavailable - cancellation request notification not sent");
    }
    
    @Override
    public void sendBookingCancelled(Map<String, Object> request) {
        System.out.println("Notification service unavailable - booking cancellation notification not sent");
    }
    
    @Override
    public void sendAdminNewBooking(Map<String, Object> request) {
        System.out.println("Notification service unavailable - admin new booking notification not sent");
    }
}