package com.example.askfmi.config;

import org.springframework.web.cors.CorsConfigurationSource;
import com.example.askfmi.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, CorsConfigurationSource corsConfigurationSource) throws Exception {
        // disable csrf since we use jwt tokens for stateless auth
        http.csrf(csrf -> csrf.disable())
                // enable cors for frontend communication
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // stateless session - no server-side session storage, only jwt validation
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // define which endpoints are public vs protected
                .authorizeHttpRequests(auth -> auth
                        // allow access to auth endpoints and ping without token
                        .requestMatchers("/ping", "/api/auth/**").permitAll()
                        // all other endpoints require valid jwt token
                        .anyRequest().authenticated()
                )
                // custom json response when authentication fails
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            // return structured error for frontend to handle
                            Map<String, String> res = new HashMap<>();
                            res.put("error", "Authentication required");
                            res.put("message", "User not authenticated");

                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(res));
                        })
                )
                // jwt filter runs before spring's username/password filter to validate tokens
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
