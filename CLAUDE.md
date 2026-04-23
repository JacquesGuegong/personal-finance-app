# CLAUDE.md

## Project
Personal Finance Tracker REST API
Java 21, Spring Boot 3, PostgreSQL, Gradle, JWT auth

## Architecture
Controller → Service → Repository → Database
- Controllers: HTTP only, no business logic
- Services: all business logic lives here
- Repositories: database access only
- DTOs: always used for API input/output, never expose entities

## Rules
- Constructor injection only (never @Autowired on fields)
- Every query must filter by the logged-in userId
- Secrets in environment variables only, never in code
- @Transactional on service methods that write to DB
- Custom exceptions for ResourceNotFound and Unauthorized

## Database
PostgreSQL · Flyway migrations in src/main/resources/db/migration

## Testing
- Unit tests: JUnit 5 + Mockito for service layer
- Integration tests: MockMvc for controllers