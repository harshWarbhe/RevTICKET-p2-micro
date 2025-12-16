package com.revticket.user.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Use a long enough key for HS256 (at least 32 bytes/256 bits)
    private final String TEST_SECRET = "TestSecretKeyForRevTicketMicroservicesProject2024!";
    private final Long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void testGenerateAndValidateToken() {
        String username = "testuser@example.com";
        String role = "USER";

        String token = jwtUtil.generateToken(username, role);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token, username));
        assertFalse(jwtUtil.validateToken(token, "other@example.com"));
    }

    @Test
    void testExtractUsername() {
        String username = "testuser@example.com";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractExpiration() {
        String username = "testuser@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(username, role);

        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testExtractRoleClaim() {
        String username = "testuser@example.com";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        Claims claims = jwtUtil.extractClaim(token, c -> c);
        assertEquals(role, claims.get("role"));
    }
}
