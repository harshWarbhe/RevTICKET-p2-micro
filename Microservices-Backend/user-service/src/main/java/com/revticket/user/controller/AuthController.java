package com.revticket.user.controller;

import com.revticket.user.dto.AuthResponse;
import com.revticket.user.dto.LoginRequest;
import com.revticket.user.dto.OAuth2LoginRequest;
import com.revticket.user.dto.ResetPasswordRequest;
import com.revticket.user.dto.SignupRequest;
import com.revticket.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            // This will be handled by GlobalExceptionHandler
            throw e;
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        authService.forgotPassword(email);
        return ResponseEntity.ok(Map.of("message", "Password reset token generated successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/oauth2/login")
    public ResponseEntity<AuthResponse> oauth2Login(@RequestBody OAuth2LoginRequest request) {
        return ResponseEntity.ok(authService.oauth2Login(request));
    }
}
