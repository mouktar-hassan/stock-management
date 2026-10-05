# Development Conventions

## Technology

- Java 17
- Spring Boot 3.5
- Maven
- Spring Data JPA
- H2 Database
- JUnit 5 and Mockito

## Architecture

Respect the following layered architecture:

Controller -> Service -> Repository

Use DTOs for API requests and responses.

Do not expose JPA entities directly through REST endpoints.

## Coding rules

- Follow Java naming conventions.
- Prefer constructor dependency injection.
- Use BigDecimal for monetary values.
- Keep business logic inside the service layer.
- Use Bean Validation for request validation.
- Handle business exceptions consistently.
- Avoid unnecessary abstractions.
- Do not introduce additional dependencies
  without justification.

## Testing

- Write unit tests for business logic.
- Include positive and negative scenarios.
- Run the existing tests before completing a task.

## Scope

- Modify only files related to the requested task.
- Preserve existing behavior.
- Do not perform unrelated refactoring.
- Explain important architectural decisions.