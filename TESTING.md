# Testing Process Documentation

This document outlines the testing process for our e-commerce microservices architecture.

## Overview

Our testing strategy follows a multi-layered approach:
1. Unit Tests
2. Integration Tests
3. End-to-End Tests
4. Manual Testing Scripts

## Unit Testing

Unit tests are written using JUnit 5 and Mockito. They focus on testing individual components in isolation.

### Running Unit Tests

For each service, unit tests can be run using Maven:

```bash
cd backend/services/<service-name>
mvn clean test
```

Example for notification service:
```bash
cd backend/services/notification-service
mvn clean test
```

## Integration Testing

Integration tests verify the interaction between different components of the system. We use:
- TestContainers for managing test dependencies (MongoDB, Kafka)
- Spring Boot Test for integration testing
- Maven Failsafe Plugin for running integration tests

### Running Integration Tests

Integration tests are located in the `src/it/java` directory of each service. To run them:

```bash
cd backend/services/<service-name>
mvn clean verify -DskipUnitTests
```

## End-to-End Testing

We have several shell scripts for end-to-end testing of our services:

### Notification Service Testing

1. Full Service Test:
```bash
./backend/scripts/test-notification-service.sh
```
This script:
- Starts required services (MongoDB, Kafka)
- Creates necessary Kafka topics
- Sends test notifications
- Verifies notification processing
- Checks MongoDB for stored notifications

2. Simple Notification Test:
```bash
./backend/scripts/test-notification.sh
```
This script sends a test notification to Kafka and verifies its processing.

### Kafka Testing

We have scripts to test Kafka connectivity and configuration:

1. Windows (PowerShell):
```powershell
./test-kafka-connection.ps1
```

2. Linux/Mac:
```bash
./test-kafka-docker.sh
```

These scripts verify:
- Kafka connectivity
- Topic creation
- Message production/consumption

## Test Structure

### Unit Tests
- Located in `src/test/java`
- Test individual components
- Use mocks for external dependencies
- Follow AAA pattern (Arrange-Act-Assert)

### Integration Tests
- Located in `src/it/java`
- Test component interactions
- Use TestContainers for external services
- Verify end-to-end flows

## Best Practices

1. **Test Isolation**
   - Each test should be independent
   - Clean up test data after each test
   - Use `@BeforeEach` and `@AfterEach` for setup/teardown

2. **Test Coverage**
   - Aim for high test coverage
   - Focus on critical business logic
   - Include edge cases and error scenarios

3. **Test Data**
   - Use meaningful test data
   - Avoid hardcoded values
   - Clean up test data after tests

4. **Continuous Integration**
   - Tests run automatically on each commit
   - Failed tests block the build
   - Regular test maintenance

## Troubleshooting

If tests fail, check:
1. Service dependencies (MongoDB, Kafka) are running
2. Network connectivity between services
3. Test data cleanup
4. Log files for detailed error messages

## Adding New Tests

When adding new tests:
1. Follow the existing test structure
2. Use appropriate test annotations
3. Include clear test descriptions
4. Add necessary test data setup
5. Clean up after tests
6. Update this documentation if needed 