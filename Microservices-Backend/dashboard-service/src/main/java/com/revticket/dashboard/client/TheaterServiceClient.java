package com.revticket.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "theater-service")
public interface TheaterServiceClient {
    
    @GetMapping("/api/admin/theaters/stats")
    Map<String, Object> getTheaterStats();
}
