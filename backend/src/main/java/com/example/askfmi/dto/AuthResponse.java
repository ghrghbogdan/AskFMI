package com.example.askfmi.dto;

public class AuthResponse {
    private String message;
    private String token;
    private UserInfo user;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String message, String token, UserInfo user) {
        this.message = message;
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    // Nested UserInfo class
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;

        public UserInfo() {}

        public UserInfo(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}