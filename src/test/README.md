# User Service Test Suite

This directory contains comprehensive unit and integration tests for the user-service.

## Test Coverage

The test suite aims for **80-90%+ code coverage** and includes:

### Unit Tests
- **AuthenticationServiceTest** - Tests for registration, login, logout, token refresh, and account locking
- **UserServiceTest** - Tests for user CRUD operations, profile management, and admin functions
- **JwtServiceTest** - Tests for JWT token generation, validation, and claim extraction
- **AddressServiceTest** - Tests for address management operations
- **EmailVerificationServiceTest** - Tests for email verification flow
- **PasswordResetServiceTest** - Tests for password reset functionality

### Integration Tests
- **AuthenticationControllerIntegrationTest** - Full HTTP integration tests for authentication endpoints
- **UserControllerIntegrationTest** - Full HTTP integration tests for user management endpoints

## Running Tests

### Run all tests
```bash
mvn test
```

### Run with coverage report
```bash
mvn clean test jacoco:report
```

Coverage report will be generated at: `target/site/jacoco/index.html`

### Run specific test class
```bash
mvn test -Dtest=AuthenticationServiceTest
```

### Run tests with specific profile
```bash
mvn test -Dspring.profiles.active=test
```

## Test Configuration

- **Test Profile**: Uses `application-test.yml` with H2 in-memory database
- **Test Data**: Uses `TestDataBuilder` utility for consistent test data
- **Mocking**: Uses Mockito for unit tests
- **Integration**: Uses `@SpringBootTest` with `MockMvc` for integration tests

## Test Utilities

- **TestDataBuilder** - Builder pattern for creating test objects
- **TestConfig** - Test-specific Spring configuration

## Code Coverage

JaCoCo plugin is configured to:
- Generate coverage reports
- Enforce minimum 80% line coverage
- Fail build if coverage is below threshold

View coverage report:
```bash
open target/site/jacoco/index.html
```

## Notes

- All tests use the `test` profile
- Redis is mocked for unit tests
- Security is disabled/mocked for integration tests where appropriate
- H2 database is used instead of PostgreSQL for faster test execution

