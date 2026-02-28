package com.example.askfmi.service;

import com.example.askfmi.dto.AuthResponse;
import com.example.askfmi.dto.LoginRequest;
import com.example.askfmi.dto.RegisterRequest;
import com.example.askfmi.entity.User;
import com.example.askfmi.repository.UserRepository;
import com.example.askfmi.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = new User("Test User", "test@example.com", "hashedPassword");
        testUser.setId(1L);
    }

    // Register Tests

    @Test
    void register_withValidData_shouldCreateUserAndReturnToken() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser.getEmail())).thenReturn("fake-jwt-token");

        AuthResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getUser());
        assertEquals("Test User", response.getUser().getName());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_withPasswordMismatch_shouldThrowException() {
        registerRequest.setConfirmPassword("differentPassword");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withExistingEmail_shouldThrowException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("This email is already in use", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldHashPassword() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser.getEmail())).thenReturn("token");

        userService.register(registerRequest);

        // Verify password was encoded
        verify(passwordEncoder).encode("password123");
        // Verify user was saved with hashed password
        verify(userRepository).save(argThat(user -> 
            user.getPassword().equals("hashedPassword")
        ));
    }

    // Login Tests

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getEmail())).thenReturn("fake-jwt-token");

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    void login_withNonExistentEmail_shouldThrowException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_withWrongPassword_shouldThrowException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void login_shouldNotRevealUserExistence() {
        // Both non-existent email and wrong password should return same error message
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            LoginRequest request = new LoginRequest();
            request.setEmail("nonexistent@example.com");
            request.setPassword("password");
            userService.login(request);
        });

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);
        
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("wrongPassword");
            userService.login(request);
        });

        // Both should have the same generic error message
        assertEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals("Invalid email or password", exception1.getMessage());
    }
}
