package com.revticket.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "showtime-service")
public interface ShowtimeServiceClient {
    
    @GetMapping("/api/admin/showtimes/stats")
    Map<String, Object> getShowtimeStats();
}
