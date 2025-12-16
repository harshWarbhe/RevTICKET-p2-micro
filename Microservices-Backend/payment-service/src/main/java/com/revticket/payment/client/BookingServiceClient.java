package com.revticket.payment.client;

import com.revticket.payment.dto.BookingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for booking service - simplified to match monolithic approach
 * userId is passed in the request body, not in headers
 */
@FeignClient(name = "booking-service", url = "http://localhost:8085")
public interface BookingServiceClient {

    @PostMapping("/api/bookings")
    Map<String, Object> createBooking(@RequestBody BookingRequest bookingRequest);
}
