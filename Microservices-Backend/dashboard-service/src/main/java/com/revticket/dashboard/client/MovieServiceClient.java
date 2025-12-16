package com.revticket.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "movie-service")
public interface MovieServiceClient {
    
    @GetMapping("/api/admin/movies/stats")
    Map<String, Object> getMovieStats();
}
