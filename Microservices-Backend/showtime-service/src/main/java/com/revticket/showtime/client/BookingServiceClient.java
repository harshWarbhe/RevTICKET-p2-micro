package com.revticket.showtime.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "booking-service")
public interface BookingServiceClient {
    
    @PostMapping("/api/seats/initialize")
    Map<String, String> initializeSeats(@RequestBody Map<String, String> request);
}