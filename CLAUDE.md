# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Golf API is a Spring Boot 3.5.7 REST service for tracking golf scorecards and calculating handicap indexes.

**Tech Stack:** Java 21, Spring Boot 3.5.7, PostgreSQL, OAuth2/JWT (Okta), Maven

## Git Workflow

**Prefer git worktrees** for feature branches to keep the main working directory clean and allow parallel work:
```bash
git worktree add .worktrees/<branch-name> -b <branch-name>
cd .worktrees/<branch-name>
```

Worktrees are stored under `.worktrees/` (gitignored). Remove when done:
```bash
git worktree remove .worktrees/<branch-name>
```

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
├── config/                # Security, JPA, async, and web configuration
├── security/              # @CanRead, @CanWrite meta-annotations for scope authorization
├── course/                # Golf course and tee CRUD operations
├── scorecard/             # Scorecard CRUD operations
├── handicap/              # Handicap index tracking and history
├── differential/          # Score differential calculation
└── errors/                # Global exception handlers (@ControllerAdvice)
```

### Domain Model

**Scorecard** - Represents a golf round score
- Fields: `teeId`, `score`, `scoreDate`, `scorecardType`, audit fields
- JPA entity with PostgreSQL persistence

**Course** - Represents a golf course
- Fields: `club`, `course`, `city`, `state`, audit fields
- Has associated `Tee` entities (blue, white, red tees, etc.)

**Tee** - Represents a set of tees for a course
- Fields: `name`, `par`, `slope`, `rating`, audit fields

**Handicap** - Tracks a golfer's current handicap index
- Fields: `golferId`, `handicapIndex`, `roundsUsed`, `totalRounds`, audit fields
- Full revision history via Hibernate Envers (`@Audited`)

### Security Model

**Authentication:** OAuth2 JWT tokens via Okta
- Resource server validates JWT signatures against configured issuer
- User identity extracted from `sub` claim in JWT

**Authorization:** User-scoped data isolation
- Every operation filters data by `SecurityContextHolder.getContext().getAuthentication().getName()`
- Pattern ensures users only see their own scorecards

**Scope-based Access Control:** Meta-annotations in `com.alimmit.golf.security`
- `@CanRead("scorecard")` → requires `SCOPE_read:scorecard` authority
- `@CanWrite("scorecard")` → requires `SCOPE_write:scorecard` authority
- `@CanRead("course")` → requires `SCOPE_read:course` authority (CourseController, TeeController)
- `@CanWrite("course")` → requires `SCOPE_write:course` authority (CourseController, TeeController)
- Uses Spring Security's `@PreAuthorize` template placeholders (`{value}`)
- Enabled by `MethodSecurityConfiguration` (`@EnableMethodSecurity` + `PrePostTemplateDefaults` bean)
- `@WebMvcTest` classes must `@Import(MethodSecurityConfiguration.class)` for method security to be enforced

**Public Endpoints:** `/v1/api-docs/**` (OpenAPI documentation)

### Database

**Migration Tool:** Flyway
- Migrations go in: `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V1__create_scorecard_table.sql`)
- Latest migration: `V5__add_course_search_vector.sql` — next migration is **V6**

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
```

**Local Setup:** Secrets managed via 1Password CLI (`op inject`)

## Testing Patterns

### Test Personas
Use `JwtPersona` utility for consistent test identities:
```java
// Available personas: GARY_GOLFER (sub: "123"), PAT_PUTTER (sub: "234"), DANA_DRIVER (sub: "345"), AMY_ADMIN (sub: "456")
// AMY_ADMIN defaults to SCOPE_READ_ACTUATOR; other personas default to DEFAULT_SCOPES (read+write for course and scorecard, read for handicap)
mockMvc.perform(post("/v1/scorecard")
    .with(jwt().jwt(JwtPersona::forGaryGolfer))
    .content(requestBody))
```

### Controller Tests
- Use `@WebMvcTest` for lightweight testing
- Active profile: "test"
- Pattern: Test multi-tenancy (users can't see each other's data)

### Service Unit Tests
- Use `@ExtendWith(MockitoExtension.class)` with `@Mock` / `@InjectMocks` — no Spring context needed
- Service classes are package-private; place tests in the same package

### Integration Tests
- Tests with `IT` suffix are integration tests (e.g., `ScorecardControllerIT`, `TeeControllerIT`)
- Use Testcontainers; Docker must be running
- UUIDv7 is hexadecimal — use `[0-9a-f]` in UUID regex patterns, not `[0-9]`
- Use `@MockitoBean` (Spring Boot 3.4+) to mock beans in `@SpringBootTest` IT tests

### Async Event Testing
For testing async Spring event pipelines (e.g., scorecard event → handicap recalculation):
- Use Awaitility (`await().atMost(...).untilAsserted(...)`) for async assertions
- Use `@ExtendWith(OutputCaptureExtension.class)` + `CapturedOutput` to assert log output
- When testing create then delete, wait for the create event to settle before issuing the delete to avoid Mockito verify race conditions

## Code Conventions

### ID Generation
All entity primary keys are UUIDs generated via PostgreSQL's `uuidv7()` function:
```java
@Id
@Generated(sql = "uuidv7()", writable = true)
@Column(name = "scorecard_id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuidv7()")
private UUID id;
```

### DTOs as Records
All DTOs use Java Records for immutability:
```java
public record ScorecardDto(String scorecardId, Long courseId, Integer score, LocalDate scoreDate, ...) {}
```

### Constants Organization
- `GlobalConstants`: API version paths (`/v1`, `/{id}`)
- Domain constants: `ScorecardConstants`, `CourseConstants`, `TeeConstants`, `HandicapConstants`
- Prevents hardcoded strings in controllers

### Exception Handling
- Custom exceptions: `NotFoundException`, `DuplicateException`
- Global handler: `GlobalControllerErrorHandler` with `@ControllerAdvice`
- Maps exceptions to HTTP status codes (404, 409, 500, etc.)
- `IllegalStateException` → 500 (e.g. delete returning more than 1 row)
- `MethodArgumentNotValidException` → 400 (triggered by `@Valid` on `@RequestBody`)
- `ConstraintViolationException` → 400 (triggered by `@NotBlank`/`@NotNull` on `@RequestParam`; requires `@Validated` on the controller class)
- `HttpMessageNotReadableException` → 400 (e.g. invalid enum values in JSON)
- `DuplicateException` → 409 (course POST/PATCH violates unique constraint on `created_by, club, course, city, state`)

### Duplicate Course Detection
- Uniqueness enforced via pre-flight service check (not JSR-303) — business logic, not input validation
- `CourseRepository.existsByUniqueConstraintForCurrentUser(...)` — used on POST
- `CourseRepository.existsByUniqueConstraintForCurrentUserExcluding(...)` — used on PATCH (excludes the course being updated to allow re-saving unchanged values)
- DB unique index `idx_unique_club_course_location` on `(created_by, club, course, city, state)` remains as the authoritative constraint
- TOCTOU fallback: `save()` is wrapped in `try/catch DataIntegrityViolationException` → re-thrown as `DuplicateException` to handle concurrent duplicate inserts that slip past the pre-flight check

## Known Issues & Future Work

**Configuration Typos:**
`application.properties` has typos in datasource property names:
- `DATASOURCE_USERNANME` (should be USERNAME)
- `DATASOURCE_PASSOWRD` (should be PASSWORD)

These match the environment variable names, so fixing requires coordinated change.

**Potential Enhancements:**
- Handicap index recalculation triggers (e.g., on scorecard submission)
- User profile management
- Course favorites/history
