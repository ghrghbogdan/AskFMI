package com.example.askfmi.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
        }

        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public String getErrorsAsString() { return String.join(", ", errors); }
    }

    public ValidationResult validateRegistration(String name, String email, String password, String confirmPassword) {
        ValidationResult result = new ValidationResult();

        // Name validation
        if (name == null || name.trim().isEmpty()) {
            result.addError("Name is required");
        } else if (name.trim().length() < 2) {
            result.addError("Name must be at least 2 characters");
        } else if (name.trim().length() > 50) {
            result.addError("Name must be no more than 50 characters");
        }

        // Email validation
        if (email == null || email.trim().isEmpty()) {
            result.addError("Email is required");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            result.addError("Email format is invalid");
        }

        // Password validation
        if(!password.equals(confirmPassword)) {
            result.addError("Passwords do not match");
        } else if (password == null || password.isEmpty()) {
            result.addError("Password is required");
        } else if (password.length() < 6) {
            result.addError("Password must be at least 6 characters");
        }

        return result;
    }

    public ValidationResult validateLogin(String email, String password) {
        ValidationResult result = new ValidationResult();

        // Email validation
        if (email == null || email.trim().isEmpty()) {
            result.addError("Email is required");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            result.addError("Email format is invalid");
        }

        // Password validation
        if (password == null || password.isEmpty()) {
            result.addError("Password is required");
        }

        return result;
    }
}