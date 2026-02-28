package com.example.askfmi.controller;

import com.example.askfmi.dto.AuthResponse;
import com.example.askfmi.dto.LoginRequest;
import com.example.askfmi.dto.RegisterRequest;
import com.example.askfmi.service.UserService;
import com.example.askfmi.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setName("Test User");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setConfirmPassword("password123");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(1L, "Test User", "test@example.com");
        mockAuthResponse = new AuthResponse("Success", "fake-jwt-token", userInfo);
    }

    // Registration Tests

    @Test
    void register_withValidData_shouldReturnOk() {
        ValidationUtil.ValidationResult validResult = new ValidationUtil.ValidationResult();
        when(validationUtil.validateRegistration(any(), any(), any(), any())).thenReturn(validResult);
        when(userService.register(any(RegisterRequest.class))).thenReturn(mockAuthResponse);

        ResponseEntity<?> response = authController.register(validRegisterRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);
        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void register_withInvalidEmail_shouldReturnBadRequest() {
        ValidationUtil.ValidationResult invalidResult = new ValidationUtil.ValidationResult();
        invalidResult.addError("Invalid email format");
        when(validationUtil.validateRegistration(any(), any(), any(), any())).thenReturn(invalidResult);

        ResponseEntity<?> response = authController.register(validRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Validation failed", body.get("error"));
        verify(userService, never()).register(any());
    }

    @Test
    void register_withPasswordMismatch_shouldReturnBadRequest() {
        validRegisterRequest.setConfirmPassword("differentPassword");
        ValidationUtil.ValidationResult validResult = new ValidationUtil.ValidationResult();
        when(validationUtil.validateRegistration(any(), any(), any(), any())).thenReturn(validResult);
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Passwords do not match"));

        ResponseEntity<?> response = authController.register(validRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Passwords do not match", body.get("error"));
    }

    @Test
    void register_withDuplicateEmail_shouldReturnConflict() {
        ValidationUtil.ValidationResult validResult = new ValidationUtil.ValidationResult();
        when(validationUtil.validateRegistration(any(), any(), any(), any())).thenReturn(validResult);
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new DataIntegrityViolationException("This email is already in use"));

        ResponseEntity<?> response = authController.register(validRegisterRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("This email is already in use", body.get("error"));
    }

    @Test
    void register_withWeakPassword_shouldReturnBadRequest() {
        ValidationUtil.ValidationResult invalidResult = new ValidationUtil.ValidationResult();
        invalidResult.addError("Password must be at least 8 characters");
        when(validationUtil.validateRegistration(any(), any(), any(), any())).thenReturn(invalidResult);

        ResponseEntity<?> response = authController.register(validRegisterRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).register(any());
    }

    // Login Tests

    @Test
    void login_withValidCredentials_shouldReturnOk() {
        ValidationUtil.ValidationResult validResult = new ValidationUtil.ValidationResult();
        when(validationUtil.validateLogin(any(), any())).thenReturn(validResult);
        when(userService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        ResponseEntity<?> response = authController.login(validLoginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("fake-jwt-token", authResponse.getToken());
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void login_withInvalidCredentials_shouldReturnUnauthorized() {
        ValidationUtil.ValidationResult validResult = new ValidationUtil.ValidationResult();
        when(validationUtil.validateLogin(any(), any())).thenReturn(validResult);
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));

        ResponseEntity<?> response = authController.login(validLoginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Invalid email or password", body.get("error"));
    }

    @Test
    void login_withEmptyEmail_shouldReturnBadRequest() {
        ValidationUtil.ValidationResult invalidResult = new ValidationUtil.ValidationResult();
        invalidResult.addError("Email is required");
        when(validationUtil.validateLogin(any(), any())).thenReturn(invalidResult);

        ResponseEntity<?> response = authController.login(validLoginRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Validation failed", body.get("error"));
        verify(userService, never()).login(any());
    }

    @Test
    void login_withEmptyPassword_shouldReturnBadRequest() {
        ValidationUtil.ValidationResult invalidResult = new ValidationUtil.ValidationResult();
        invalidResult.addError("Password is required");
        when(validationUtil.validateLogin(any(), any())).thenReturn(invalidResult);

        ResponseEntity<?> response = authController.login(validLoginRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).login(any());
    }

    @Test
    void login_withNonExistentUser_shouldReturnUnauthorized() {
        ValidationUtil.ValidationResult validResult = new ValidationUtil.ValidationResult();
        when(validationUtil.validateLogin(any(), any())).thenReturn(validResult);
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));

        ResponseEntity<?> response = authController.login(validLoginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void login_withWrongPassword_shouldReturnUnauthorized() {
        validLoginRequest.setPassword("wrongPassword");
        ValidationUtil.ValidationResult validResult = new ValidationUtil.ValidationResult();
        when(validationUtil.validateLogin(any(), any())).thenReturn(validResult);
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));

        ResponseEntity<?> response = authController.login(validLoginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Invalid email or password", body.get("error"));
    }
}
