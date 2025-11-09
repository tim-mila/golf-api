# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Golf API is a Spring Boot 3.5.7 REST service for tracking golf scorecards and calculating handicap indexes. It integrates with an external golf course API to fetch course ratings and hole information.

**Tech Stack:** Java 21, Spring Boot 3.5.7, PostgreSQL, OAuth2/JWT (Okta), Maven

## Development Commands

### Local Development
```bash
./local-setup    # Initialize environment (uses 1Password CLI to inject secrets)
./local-start    # Start PostgreSQL Docker container and run Spring Boot app
```

The `local-start` script:
1. Sources environment variables from `.local/.env.public`
2. Starts PostgreSQL container on port 9876 via Docker Compose
3. Runs Spring Boot with `./mvnw spring-boot:run`

### Building
```bash
./mvnw clean install              # Full build with tests
./mvnw clean package              # Build without running tests
./mvnw spring-boot:run            # Run the application
```

### Testing
```bash
./mvnw test                                    # Run all tests (uses Testcontainers)
./mvnw test -Dtest=ScorecardControllerTest     # Run single test class
./mvnw test -Dtest=ScorecardControllerTest#testMethodName  # Run single test method
```

**Important:** Tests use Testcontainers with PostgreSQL 17.6, which automatically starts/stops a Docker container. Docker must be running.

### API Documentation
- Swagger UI: http://localhost:8080/v1/api-docs/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v1/api-docs

## Architecture

### Package Structure
```
com.alimmit.golf
├── security/              # OAuth2/JWT config, OpenAPI security definitions
├── courses/               # Golf course lookup endpoints
│   └── client/           # External golf course API client (golfcourseapi.com)
├── scorecard/            # Scorecard CRUD operations
├── errors/               # Global exception handlers (@ControllerAdvice)
└── id/                   # ID generation (timestamp + SecureRandom mixing)
```

### Domain Model

**Scorecard** - Represents a golf round score
- ID format: `scr-{32-hex-chars}` (timestamp-based + cryptographically random)
- Fields: `courseId`, `score`, `scoreDate`, audit fields
- Currently stored in-memory (keyed by user ID from JWT)
- **Planned:** JPA entity with PostgreSQL persistence

**Golf Course** - External data from golfcourseapi.com
- Includes course ratings, slope ratings, hole-by-hole data
- Fetched on-demand (no local caching yet)
- Client: `DefaultGolfCourseApiClientImpl` using Java `HttpClient`

### Security Model

**Authentication:** OAuth2 JWT tokens via Okta
- Resource server validates JWT signatures against configured issuer
- User identity extracted from `sub` claim in JWT

**Authorization:** User-scoped data isolation
- Every operation filters data by `SecurityContextHolder.getContext().getAuthentication().getName()`
- Pattern ensures users only see their own scorecards

**Public Endpoints:** `/v1/api-docs/**` (OpenAPI documentation)

### External Integrations

**Golf Course API (golfcourseapi.com)**
- Authentication: `Authorization: Key {api-key}` header
- Endpoints:
  - `GET /v1/search?search_query={terms}` - Search courses
  - `GET /v1/courses/{id}` - Fetch course details
- Client interface: `GolfCourseApiClient` (allows easy mocking)
- Configuration properties: `golf.course-api.url`, `golf.course-api.api-key`

### Database

**Current State:** Database dependencies configured but not yet used (in-memory storage only)

**Migration Tool:** Flyway
- Migrations go in: `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V1__create_scorecard_table.sql`)

**Local Database:** PostgreSQL 17.6 via Docker Compose
- Container: `golf-postgres`
- Port: 9876 (maps to 5432 inside container)
- Connection URL: `jdbc:postgresql://localhost:9876/golf`

**Test Database:** Testcontainers with PostgreSQL 17.6
- Uses JDBC URL: `jdbc:tc:postgresql:17.6:///databasename`
- Automatic container lifecycle per test class

### Configuration

**Required Environment Variables:**
```bash
# Database (note: typos exist in application.properties)
DATASOURCE_URL=jdbc:postgresql://localhost:9876/golf
DATASOURCE_USERNANME=postgres         # Note: typo in property name
DATASOURCE_PASSOWRD=password          # Note: typo in property name

# OAuth2 (Okta/Auth0)
AUTH0_ISSUER_URL=https://your-tenant.auth0.com/
AUTH0_AUDIENCE=https://your-api-audience

# External Golf Course API
GOLF_COURSE_API_URL=https://api.golfcourseapi.com
GOLF_COURSE_API_KEY=your-api-key
```

**Local Setup:** Secrets managed via 1Password CLI (`op inject`)

## Testing Patterns

### Test Personas
Use `JwtPersona` utility for consistent test identities:
```java
// Available personas: GARY_GOLFER (sub: "123"), PAT_PUTTER (sub: "234"), DANA_DRIVER (sub: "345")
mockMvc.perform(post("/v1/scorecard")
    .with(jwt().jwt(JwtPersona::forGaryGolfer))
    .content(requestBody))
```

### Controller Tests
- Use `@WebMvcTest` for lightweight testing
- Mock external dependencies (e.g., `GolfCourseApiClient`)
- Active profile: "test"
- Pattern: Test multi-tenancy (users can't see each other's data)

### Integration Tests
- Tests with `IT` suffix are integration tests
- Example: `DefaultGolfCourseApiClientImplIT` tests real external API
- Require environment variable: `GOLF_COURSE_API_KEY`

## Code Conventions

### ID Generation
All domain IDs use the `IdGenerator` base class pattern:
- Combines `System.currentTimeMillis()` + `SecureRandom` with XOR mixing
- Subclasses define prefix (e.g., `ScorecardIdGenerator` → "scr-")
- Format: `{prefix}{32-hex-chars}` (36 chars total for "scr-" prefix)

### DTOs as Records
All DTOs use Java Records for immutability:
```java
public record ScorecardDto(String scorecardId, Long courseId, Integer score, LocalDate scoreDate, ...) {}
```

### Constants Organization
- `GlobalConstants`: API version paths (`/v1`, `/{id}`)
- Domain constants: `ScorecardConstants`, `GolfCourseConstants`
- Prevents hardcoded strings in controllers

### Configuration Properties
Use `@ConfigurationProperties` with constructor binding:
```java
@ConfigurationProperties(prefix = "golf.course-api")
public class GolfCourseApiConfigurationProperties {
    private final String url;
    private final String apiKey;
    // ...
}
```

### Exception Handling
- Custom exceptions: `NotFoundException`, `GolfCourseApiException`
- Global handler: `GlobalControllerErrorHandler` with `@ControllerAdvice`
- Maps exceptions to HTTP status codes (404, 500, etc.)

## Known Issues & Future Work

**Database Migration Needed:**
The comment in `ScorecardController.java:87` indicates:
```java
throw new IllegalStateException("This won't happen once there's a DB");
```
Scorecards are currently in-memory. Planned work includes:
- Create JPA entities for `Scorecard`
- Write Flyway migrations
- Implement repository layer

**Configuration Typos:**
`application.properties` has typos in datasource property names:
- `DATASOURCE_USERNANME` (should be USERNAME)
- `DATASOURCE_PASSOWRD` (should be PASSWORD)

These match the environment variable names, so fixing requires coordinated change.

**Potential Enhancements:**
- Golf course data caching (reduce external API calls)
- Handicap index calculation algorithms
- User profile management
- Course favorites/history
