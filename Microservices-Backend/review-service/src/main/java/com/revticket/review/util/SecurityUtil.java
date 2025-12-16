package com.revticket.review.util;

import com.revticket.review.client.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SecurityUtil {

    @Autowired
    private UserServiceClient userServiceClient;

    public String getCurrentUserId(Authentication authentication, String token) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }

        try {
            Map<String, Object> user = userServiceClient.getUserProfile(token);
            return (String) user.get("id");
        } catch (Exception e) {
            throw new RuntimeException("User not found");
        }
    }
}
