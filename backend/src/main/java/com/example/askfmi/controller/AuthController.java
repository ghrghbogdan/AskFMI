package com.example.askfmi.controller;

import com.example.askfmi.dto.AuthResponse;
import com.example.askfmi.dto.LoginRequest;
import com.example.askfmi.dto.RegisterRequest;
import com.example.askfmi.service.UserService;
import com.example.askfmi.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ValidationUtil validationUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // validate input data before processing - checks email format, password strength, etc
            ValidationUtil.ValidationResult validation = validationUtil.validateRegistration(
                request.getName(), request.getEmail(), request.getPassword(), request.getConfirmPassword());
            
            if (!validation.isValid()) {
                // return all validation errors to frontend for display
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Validation failed");
                errorResponse.put("message", validation.getErrorsAsString());
                errorResponse.put("details", validation.getErrors());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // create user, hash password, generate jwt token
            AuthResponse response = userService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // password mismatch or invalid input from service layer
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (DataIntegrityViolationException e) {
            // database constraint violation - usually duplicate email (unique constraint)
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "This email is already in use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            // catch-all for unexpected errors
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // validate email and password fields are not empty
            ValidationUtil.ValidationResult validation = validationUtil.validateLogin(
                request.getEmail(), request.getPassword());
            
            if (!validation.isValid()) {
                // return field-level errors to frontend
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Validation failed");
                errorResponse.put("message", validation.getErrorsAsString());
                errorResponse.put("details", validation.getErrors());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // verify credentials with spring security and generate jwt token
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // invalid credentials or user not found - return 401 unauthorized
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}