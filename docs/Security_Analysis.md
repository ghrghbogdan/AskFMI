# Security Analysis

## Password Storage

Passwords are hashed using BCrypt with 10 rounds before being stored in the database. This means even if the database is compromised, passwords cannot be reversed to plaintext.

Implementation in `PasswordEncoderConfig.java`:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

The hashing is tested in `PasswordEncoderTest.java` to ensure it works correctly.

## Authentication

The application uses JWT (JSON Web Tokens) for stateless authentication. When a user logs in, they receive a token that must be included in the Authorization header for all subsequent requests.

Tokens are signed using HS256 and expire after 24 hours. The implementation is in `JwtUtil.java` and tokens are validated by `JwtAuthenticationFilter.java` on every request.

Since authentication is stateless, the server doesn't maintain sessions. This makes the application more scalable and harder to exploit with session-based attacks.

## CORS Configuration

Cross-Origin Resource Sharing is configured to only accept requests from the frontend domain. This prevents unauthorized websites from making requests to the API.

Configuration in `CorsConfig.java`:
```java
configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
```

## SQL Injection Prevention

All database queries use JPA/Hibernate with parameterized queries. User input is never concatenated into SQL strings, which prevents SQL injection attacks.

Example:
```java
// Safe - uses parameterized query
userRepository.findByEmail(email);
```

## XSS Prevention

React automatically escapes all output in JSX, so user-provided content cannot execute as JavaScript code. This prevents cross-site scripting attacks.

## API Endpoint Protection

Only authentication endpoints (`/api/auth/login` and `/api/auth/register`) are publicly accessible. All other endpoints require a valid JWT token.

Configuration in `SecurityConfig.java`:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .anyRequest().authenticated()
)
```

Requests without a valid token receive a 401 Unauthorized response.

## Input Validation

User input is validated on the backend before processing. This includes checking for required fields, email format, and other constraints.

Error messages are returned to the frontend when validation fails.

## Environment Variables

Sensitive configuration like database credentials and JWT signing keys are stored in environment variables, not hardcoded in the source code.

## Testing

Security features are tested through:
- Unit tests for password hashing (`PasswordEncoderTest.java`)
- Unit tests for JWT validation (`JwtUtilTest.java`)
- Frontend tests for authentication flows (`Login.test.js`, `Register.test.js`)

Manual verification includes checking that passwords are hashed in the database and that unauthorized requests are rejected.

## Potential Improvements

Future enhancements could include:
- Rate limiting to prevent brute force attacks
- Storing tokens in HttpOnly cookies instead of localStorage
- Content Security Policy headers
- Password strength requirements
- Two-factor authentication

These are not critical for the current scope but could be added later.
