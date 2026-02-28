package com.example.askfmi.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void encode_shouldHashPassword() {
        String rawPassword = "mySecretPassword123";
        
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(hashedPassword);
        assertNotEquals(rawPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$")); // BCrypt prefix
    }

    @Test
    void matches_shouldReturnTrueForCorrectPassword() {
        String rawPassword = "testPassword";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
        
        assertTrue(matches);
    }

    @Test
    void matches_shouldReturnFalseForWrongPassword() {
        String rawPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        boolean matches = passwordEncoder.matches(wrongPassword, hashedPassword);
        
        assertFalse(matches);
    }

    @Test
    void encode_shouldProduceDifferentHashesForSamePassword() {
        String password = "samePassword";
        
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);
        
        assertNotEquals(hash1, hash2); // BCrypt uses random salt
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));
    }
}
