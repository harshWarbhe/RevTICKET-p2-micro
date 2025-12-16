package com.revticket.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "booking-service")
public interface BookingServiceClient {
    
    @GetMapping("/api/admin/bookings/stats")
    Map<String, Object> getBookingStats();
}
