package com.revticket.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "booking-service", url = "http://localhost:8085")
public interface BookingConfirmationClient {
    
    @PostMapping("/api/bookings/{id}/confirm-payment")
    Map<String, Object> confirmPayment(@PathVariable("id") String bookingId, 
                                     @RequestParam String transactionId);
}