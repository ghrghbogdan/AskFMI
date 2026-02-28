package com.example.askfmi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // allow frontend running on port 3000 to make requests to backend
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        
        // allow these http methods for api calls
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // allow all headers including authorization bearer token
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // allow credentials like cookies and authorization headers
        configuration.setAllowCredentials(true);
        
        // cache preflight requests for 1 hour to reduce overhead
        configuration.setMaxAge(3600L);

        // apply this cors config to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
