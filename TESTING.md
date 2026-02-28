# Testing Setup

This project uses automated testing with GitHub Actions CI/CD.

## Backend Tests (Java/JUnit)

Location: `backend/src/test/java/`

Run tests:
```bash
cd backend
./gradlew test
```

Run with coverage:
```bash
./gradlew test jacocoTestReport
```

Coverage report: `backend/build/reports/jacoco/test/html/index.html`

## Frontend Tests (Jest/React Testing Library)

Location: `frontend/src/__tests__/`

Run tests:
```bash
cd frontend
npm test
```

Run with coverage:
```bash
npm test -- --coverage --watchAll=false
```

Coverage report: `frontend/coverage/lcov-report/index.html`

## GitHub Actions

Workflow file: `.github/workflows/test.yml`

Runs automatically on:
- Push to `main` or `develop` branch
- Pull requests to `main` or `develop`

What it tests:
- Backend unit tests (JUnit)
- Frontend component tests (Jest)
- Test coverage reports uploaded as artifacts

## Test Files

### Backend
- `JwtUtilTest.java` - JWT token generation and validation
- `ChatServiceTest.java` - Conversation and message handling
- `PasswordEncoderTest.java` - BCrypt password hashing

### Frontend
- `Login.test.js` - Login form and authentication
- `ChatArea.test.js` - Message display and input
- `api.test.js` - Axios interceptors

## Running Tests Before Commit

```bash
# Full test suite
./run-all-tests.sh  # if you create this script

# Or manually:
cd backend && ./gradlew test && cd ../frontend && npm test -- --watchAll=false
```
