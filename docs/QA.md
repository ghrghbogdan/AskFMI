# QA Testing

## Overview

The askFMI project has automated tests for the backend (Spring Boot) and frontend (React). The AI service is tested manually since it requires GPU resources.

## Backend Tests

We use JUnit 5 for testing the Java backend.

Tests cover:
- User registration and login
- JWT token generation and validation  
- Password hashing with BCrypt
- Conversation and message persistence

Test location: `backend/src/test/java/com/example/askfmi/`

Key test files:
- `ChatServiceTest.java`
- `PasswordEncoderTest.java`
- `JwtUtilTest.java`

Run backend tests:
```bash
cd backend
./gradlew test
```

Generate coverage report:
```bash
./gradlew test jacocoTestReport
```

Coverage is around 65% for the service layer.

## Frontend Tests

We use Jest and React Testing Library for component testing.

Tests cover:
- Login and registration forms
- Form validation (password matching, etc)
- Message display in chat interface
- API request handling with auth tokens

Test location: `frontend/src/__tests__/`

Test files:
- `Login.test.js`
- `Register.test.js`
- `ChatArea.test.js`
- `api.test.js`

Run frontend tests:
```bash
cd frontend
npm test
```

Run with coverage:
```bash
npm test -- --coverage --watchAll=false
```

Coverage is around 55% for React components.

## Continuous Integration

GitHub Actions runs all tests automatically on every push and pull request.

Workflow: `.github/workflows/test.yml`

Triggers on:
- Push to main or develop branch
- Pull requests to main or develop

The workflow runs both backend and frontend tests and uploads coverage reports as artifacts. Total runtime is around 2 minutes.

## Running All Tests

Use the convenience script:
```bash
./run-all-tests.sh
```

This runs both backend and frontend test suites and shows a summary.

## Test Approach

We use unit tests for individual functions and integration tests for testing multiple components together. Mocking is used to avoid dependencies on external services (AI API, etc).

Current test count: 17 total (13 backend, 4 frontend)

## What's Not Tested

- AI service (requires GPU, tested manually)
- Database migrations
- End-to-end flows
- Performance and load testing
