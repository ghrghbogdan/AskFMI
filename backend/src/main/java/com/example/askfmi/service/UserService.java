package com.example.askfmi.service;

import com.example.askfmi.dto.AuthResponse;
import com.example.askfmi.dto.LoginRequest;
import com.example.askfmi.dto.RegisterRequest;
import com.example.askfmi.entity.User;
import com.example.askfmi.repository.UserRepository;
import com.example.askfmi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DataIntegrityViolationException("This email is already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            User savedUser = userRepository.save(user);
            
            // Generate JWT token for auto-login
            String token = jwtUtil.generateToken(savedUser.getEmail());
            
            // Return response with token and user info
            return new AuthResponse(
                "User registered successfully",
                token,
                new AuthResponse.UserInfo(savedUser.getId(), savedUser.getName(), savedUser.getEmail())
            );
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("This email is already in use");
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        
        return new AuthResponse(
            "Login successful",
            token,
            new AuthResponse.UserInfo(user.getId(), user.getName(), user.getEmail())
        );
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}