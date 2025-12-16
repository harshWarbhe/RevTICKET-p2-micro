package com.revticket.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", url = "http://localhost:8089", fallback = NotificationServiceClientFallback.class)
public interface NotificationServiceClient {
    
    @PostMapping("/api/notifications/booking-confirmation")
    void sendBookingConfirmation(@RequestBody Map<String, Object> request);
    
    @PostMapping("/api/notifications/cancellation-request")
    void sendCancellationRequest(@RequestBody Map<String, Object> request);
    
    @PostMapping("/api/notifications/booking-cancelled")
    void sendBookingCancelled(@RequestBody Map<String, Object> request);
    
    @PostMapping("/api/notifications/admin/new-booking")
    void sendAdminNewBooking(@RequestBody Map<String, Object> request);
}