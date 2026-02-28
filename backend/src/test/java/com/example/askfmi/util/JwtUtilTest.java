package com.example.askfmi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set test values using reflection since these are normally injected
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "testsecrettestsecrettestsecrettestsecret");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    void generateToken_shouldCreateNonEmptyToken() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void getEmailFromToken_shouldReturnCorrectEmail() {
        String email = "user@test.com";
        String token = jwtUtil.generateToken(email);
        
        String extractedEmail = jwtUtil.getEmailFromToken(token);
        
        assertEquals(email, extractedEmail);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken("valid@test.com");
        
        boolean isValid = jwtUtil.validateToken(token);
        
        assertTrue(isValid);
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        boolean isValid = jwtUtil.validateToken(invalidToken);
        
        assertFalse(isValid);
    }

    @Test
    void validateToken_shouldReturnFalseForNullToken() {
        boolean isValid = jwtUtil.validateToken(null);
        
        assertFalse(isValid);
    }
}
