package com.revticket.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "user-service", configuration = com.revticket.review.config.FeignConfig.class)
public interface UserServiceClient {
    @GetMapping("/api/users/profile")
    Map<String, Object> getUserProfile(@RequestHeader("Authorization") String token);

}
