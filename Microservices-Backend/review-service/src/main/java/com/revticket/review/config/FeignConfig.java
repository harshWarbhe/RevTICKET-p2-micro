package com.revticket.review.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    
                    String userId = request.getHeader("X-User-Id");
                    String userRole = request.getHeader("X-User-Role");
                    String authorization = request.getHeader("Authorization");
                    
                    if (userId != null) {
                        template.header("X-User-Id", userId);
                    }
                    if (userRole != null) {
                        template.header("X-User-Role", userRole);
                    }
                    if (authorization != null) {
                        template.header("Authorization", authorization);
                    }
                }
                
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof String) {
                        if (!template.headers().containsKey("X-User-Id")) {
                            template.header("X-User-Id", (String) principal);
                        }
                    }
                    
                    if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
                        String role = authentication.getAuthorities().iterator().next().getAuthority();
                        if (role.startsWith("ROLE_")) {
                            role = role.substring(5);
                        }
                        if (!template.headers().containsKey("X-User-Role")) {
                            template.header("X-User-Role", role);
                        }
                    }
                }
            }
        };
    }
}
