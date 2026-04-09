# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Golf API is a Spring Boot 4.0.3 REST service for tracking golf scorecards and calculating handicap indexes.

**Tech Stack:** Java 21, Spring Boot 4.0.3 (Spring Framework 7, Spring Security 7, Hibernate ORM 7, Jackson 3), PostgreSQL, OAuth2/JWT (Auth0), Maven

## Claude Code Skills

Project-specific skills are stored in `.claude/skills/` and committed to source control.

- **start-task** — starts a new development task from a GitHub issue (sets up branch/worktree and produces an implementation plan)
- **arch-review** — performs a holistic architectural review across application design, test architecture, security (OWASP Top 10), and DevOps/release; automatically files GitHub issues for every new finding

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
cd api
./local-setup    # Initialize environment (uses 1Password CLI to inject secrets)
./local-start    # Start PostgreSQL Docker container and run Spring Boot app
```

The `local-start` script:
1. Sources environment variables from `.local/.env.public`
2. Starts PostgreSQL container on port 9876 via Docker Compose
3. Runs Spring Boot with `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`

The `local` Spring profile activates `application-local.properties`, which sets `logging.level.com.alimmit.golf=debug`. The default (`application.properties`) uses `info` to prevent debug log output in production.

### Building
```bash
cd api
./mvnw clean install              # Full build with tests
./mvnw clean package              # Build without running tests
./mvnw spring-boot:run            # Run the application
./mvnw verify                     # Full build: runs unit tests, IT tests (Failsafe), and generates open-api.json at repo root
```

### Testing
```bash
cd api
./mvnw test                                    # Run all tests (uses Testcontainers)
./mvnw test -Dtest=ScorecardControllerTest     # Run single test class
./mvnw test -Dtest=ScorecardControllerTest#testMethodName  # Run single test method
```

**Important:** Tests use Testcontainers with PostgreSQL 17.6, which automatically starts/stops a Docker container. Docker must be running.

### API Documentation
- Swagger UI: http://localhost:8080/v1/api-docs/swagger-ui/index.html
- OpenAPI JSON (runtime): http://localhost:8080/v1/api-docs
- OpenAPI JSON (build artifact): `open-api.json` at repo root — generated automatically by `springdoc-openapi-maven-plugin` during `cd api && ./mvnw verify`

**Any PR that changes a controller's endpoints, request/response types, or OpenAPI annotations must regenerate `open-api.json` before merging:**
```bash
cd api && ./mvnw verify
git add open-api.json
```
This includes changes to: URL paths, HTTP methods, `@RequestParam`/`@PathVariable`/`@RequestBody` shape, `@Schema`/`@Operation`/`@ApiResponse`/`@Parameter` annotations, and request/response record fields.

### Docker
```bash
docker build -f api/Dockerfile -t golf-api .    # Build API image (run from repo root)
```

### CI/CD Workflows
- `api-test.yml` — runs `cd api && ./mvnw verify` on pull requests touching `api/**`
- `api-release.yml` — triggers on push to `main` touching `api/**`; runs tests first, then bumps semver tag, builds and pushes Docker image to GHCR, and creates a GitHub Release
- Docker image is only published if tests pass — the `Run tests` step gates all downstream release steps

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
- All tee endpoints are flat under `/v1/tee`: `GET /v1/tee?courseId=`, `POST /v1/tee` (courseId in body), `GET/PATCH/DELETE /v1/tee/{id}`
- `CreateTeeRequest` includes `courseId` — the controller passes it to `TeeService.create(UUID courseId, CreateTeeRequest)`

**Handicap** - Tracks a golfer's current handicap index
- Fields: `golferId`, `handicapIndex`, `roundsUsed`, `totalRounds`, audit fields
- Full revision history via Hibernate Envers (`@Audited`)
- `GET /v1/handicap` always returns 200 with `HandicapDto`; check `established: boolean` to determine if the 54-hole WHS threshold has been met — all other fields are null when `established: false`
- `HandicapDto.unestablished()` is the static factory for the not-yet-established state

### Security Model

**Authentication:** OAuth2 JWT tokens via Auth0 (native Spring Security resource server — no Okta starter)
- Resource server validates JWT signatures against configured issuer
- User identity extracted from `sub` claim in JWT
- Properties: `spring.security.oauth2.resourceserver.jwt.issuer-uri` / `.audiences` / `.jwk-set-uri`

**Authorization:** User-scoped data isolation
- Every operation filters data by `SecurityContextHolder.getContext().getAuthentication().getName()`
- Pattern ensures users only see their own scorecards

**Scope-based Access Control:** Meta-annotations in `com.alimmit.golf.security`
- `@CanRead("scorecard")` → requires `SCOPE_read:scorecard` authority
- `@CanWrite("scorecard")` → requires `SCOPE_write:scorecard` authority
- `@CanRead("course")` → requires `SCOPE_read:course` authority (CourseController, TeeController)
- `@CanWrite("course")` → requires `SCOPE_write:course` authority (CourseController, TeeController)
- Uses Spring Security's `@PreAuthorize` template placeholders (`{value}`)
- Enabled by `MethodSecurityConfiguration` (`@EnableMethodSecurity` + `AnnotationTemplateExpressionDefaults` bean — Spring Security 7 replacement for `PrePostTemplateDefaults`)
- `@WebMvcTest` classes must `@Import(MethodSecurityConfiguration.class)` for method security to be enforced

**Internal Event Services:** Cross-user queries must NOT be placed on public service interfaces (OWASP A01)
- `ScorecardQueryService` / `JpaScorecardQueryServiceImpl` — internal-only service for fetching scorecards by userId; used exclusively by `ScorecardEventListener` for handicap recalculation; not injected by any controller
  - `findMostRecentForUser(String userId, int limit)` — returns the most recent N scorecards ordered by `scoreDate DESC`; called by `ScorecardEventListener` with `HandicapConstants.HANDICAP_LOOKBACK_ROUNDS` (20)
- Do not add cross-user query methods to `ScorecardService` — the public interface is scoped to the current authenticated user only

**Public Endpoints:** `/v1/api-docs/**` (OpenAPI documentation)

**CORS:** Configured in `WebSecurityConfiguration` via a `CorsConfigurationSource` bean wired into the security filter chain with `.cors(...)`. Settings are bound from a `CorsProperties` record (`@ConfigurationProperties(prefix = "cors")`); all four fields are environment-configurable:
- `CORS_ALLOWED_ORIGINS` — required (`@NotEmpty`), comma-separated list of allowed origins
- `CORS_ALLOWED_METHODS` — default: `GET,POST,PATCH,DELETE,OPTIONS`
- `CORS_ALLOWED_HEADERS` — default: `Authorization,Content-Type`
- `CORS_MAX_AGE` — default: `3600` (seconds)

### Database

**Migration Tool:** Flyway
- Migrations go in: `api/src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V1__create_scorecard_table.sql`)
- Latest migration: `V6__remove_tee_yardage.sql` — next migration is **V7**

**Local Database:** PostgreSQL 17.6 via Docker Compose
- Container: `golf-postgres`
- Port: 9876 (maps to 5432 inside container)
- Connection URL: `jdbc:postgresql://localhost:9876/golf`

**Test Database:** Testcontainers with PostgreSQL 18.1
- Uses JDBC URL: `jdbc:tc:postgresql:18.1:///databasename`
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

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000   # Comma-separated list of allowed origins
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
- Use `@WebMvcTest` for lightweight testing — import from `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` (Spring Boot 4 modular package)
- Active profile: "test"
- Pattern: Test multi-tenancy (users can't see each other's data)

### Service Unit Tests
- Use `@ExtendWith(MockitoExtension.class)` with `@Mock` / `@InjectMocks` — no Spring context needed
- Service classes are package-private; place tests in the same package

### Integration Tests
- Tests with `IT` suffix are integration tests (e.g., `ScorecardControllerIT`, `TeeControllerIT`)
- Run via `./mvnw verify` (Maven Failsafe plugin); `./mvnw test` only runs `*Test.java` classes
- Use Testcontainers; Docker must be running
- UUIDv7 is hexadecimal — use `[0-9a-f]` in UUID regex patterns, not `[0-9]`
- Use `@MockitoBean` to mock beans in `@SpringBootTest` IT tests
- Spring Boot 4 modular import packages: `@AutoConfigureMockMvc` → `org.springframework.boot.webmvc.test.autoconfigure`, `@AutoConfigureTestDatabase` → `org.springframework.boot.jdbc.test.autoconfigure`, `@DataJpaTest` → `org.springframework.boot.data.jpa.test.autoconfigure`

### Async Event Testing
For testing async Spring event pipelines (e.g., scorecard event → handicap recalculation):
- Use Awaitility (`await().atMost(...).untilAsserted(...)`) for async assertions
- Use `@ExtendWith(OutputCaptureExtension.class)` + `CapturedOutput` to assert log output
- When testing create then delete, wait for the create event to settle before issuing the delete to avoid Mockito verify race conditions

### HttpMessageNotReadableException tests
- `ScorecardType` has no `@JsonCreator` — submitting an invalid enum value (e.g. `"BOGUS"`) triggers standard Jackson deserialization failure → `HttpMessageNotReadableException` → 400
- `USState` has a `@JsonCreator` that throws `IllegalArgumentException("Invalid state abbreviation: ...")` — Jackson wraps this as `HttpMessageNotReadableException` → handler extracts root cause message → 400

### TOCTOU DataIntegrityViolationException fallback
- `JpaCourseServiceImpl.create()` and `patch()` catch `DataIntegrityViolationException` from `save()` and rethrow as `DuplicateException`
- This fallback is unit-tested in `JpaCourseServiceImplTest` — if Hibernate 7 changes the exception hierarchy this will surface immediately

### Course search sanitization
- `JpaCourseServiceImpl.search()` strips `[^\w\s]`, returns `List.of()` for blank-after-sanitize (no exception, no repository call)
- tsquery is built without lowercasing: `"Augusta National"` → `"Augusta:* & National:*"`
- `"!@#$%"` is NOT blank (passes `@NotBlank` at the controller) — the controller returns 200 with `[]`; the sanitization-to-blank behavior is tested at the service unit test level

## Code Conventions

### ID Generation
All entity primary keys are UUIDs generated via PostgreSQL's `uuidv7()` function:
```java
@Id
@Generated(event = EventType.INSERT)  // Hibernate 7: re-reads DB-generated value after INSERT
@Column(name = "scorecard_id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuidv7()")
private UUID id;
```
The `columnDefinition` triggers the PostgreSQL `uuidv7()` function; `@Generated(event = EventType.INSERT)` tells Hibernate 7 to re-read the generated value post-insert. The old `@Generated(sql = "uuidv7()", writable = true)` form was removed in Hibernate 7.

### DTOs as Records
All DTOs use Java Records for immutability:
```java
public record ScorecardDto(String scorecardId, Long courseId, Integer score, LocalDate scoreDate, ...) {}
```

### OpenAPI Documentation
All DTOs have class-level and field-level `@Schema` annotations for Swagger UI / OpenAPI JSON generation:
- Class-level: `@Schema(description = "...")` on the record declaration
- Field-level: `@Schema(description = "...", example = "...")` on each record component
- Constraint fields (e.g. slope rating): include `minimum` and `maximum` attributes
- All controllers use `@ApiResponse` for each status code: 200/201/204, 400, 401, 403, 404, and 409 where applicable (exception: `HandicapController.getHandicap` uses 200-only — no 204)
- 204 No Content responses use `content = @Content` (no schema) to suppress a spurious body in the Swagger UI
- Array responses use `content = @Content(array = @ArraySchema(schema = @Schema(implementation = Foo.class)))`

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
