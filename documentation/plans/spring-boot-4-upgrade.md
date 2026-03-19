# Spring Boot 4.0.x Upgrade Plan

## Context
The project currently runs Spring Boot 3.5.10. Upgrading to 4.0.x pulls in Spring Framework 7, Spring Security 7, and Hibernate ORM 7. The codebase is already well-positioned for the upgrade (jakarta.* imports, modern SecurityFilterChain DSL, @EnableMethodSecurity) — the main work is replacing the Okta starter (incompatible with Boot 4.x), upgrading springdoc, and handling a few runtime property and type changes.

---

## Step 1 — Bump the Spring Boot parent version

**File:** `pom.xml`

```xml
<!-- was 3.5.10 -->
<version>4.0.3</version>
```

Latest GA as of 2026-03-17: **4.0.3**. Java version stays at 21.

---

## Step 2 — Remove Okta starter; replace with native Spring Security properties

`okta-spring-boot-starter:3.0.5` targets Spring Boot 3.x and has no Boot 4.x release. The app already has a complete manual `WebSecurityConfiguration` (resource server + JWT). The starter's only contribution is mapping `okta.oauth2.*` properties into Spring Security's own namespace.

**`pom.xml` — remove both starters:**
```xml
<!-- Remove: -->
<dependency>com.okta.spring:okta-spring-boot-starter:3.0.5</dependency>
<dependency>org.springframework.boot:spring-boot-starter-oauth2-client</dependency>
```
The app is a pure resource server; there is no OAuth2 client flow.

**`src/main/resources/application.properties` — replace okta properties:**
```properties
# Remove:
okta.oauth2.issuer=${AUTH0_ISSUER_URL}
okta.oauth2.audience=${AUTH0_AUDIENCE}

# Add:
spring.security.oauth2.resourceserver.jwt.issuer-uri=${AUTH0_ISSUER_URL}
spring.security.oauth2.resourceserver.jwt.audiences=${AUTH0_AUDIENCE}
```

**`src/test/resources/application-test.properties` — same replacement, plus bypass OIDC discovery:**
```properties
# Remove:
okta.oauth2.issuer=https://mock-issuer
okta.oauth2.audience=https://mock-audience

# Add:
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://mock-issuer
spring.security.oauth2.resourceserver.jwt.audiences=https://mock-audience
# Prevents Spring from calling https://mock-issuer/.well-known/openid-configuration at context startup:
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://mock-issuer/protocol/openid-connect/certs
```
The `jwt()` test support (`SecurityMockMvcRequestPostProcessors`) short-circuits actual JWT validation, so this mock URI is never contacted.

---

## Step 3 — Upgrade springdoc-openapi

springdoc 2.x targets Spring Boot 3.x; Spring Boot 4.0 requires springdoc 3.x.

**`pom.xml`:**
```xml
<!-- was 2.8.13 -->
<org.springdoc.version>3.0.2</org.springdoc.version>
```
Latest GA as of 2026-03-17: **3.0.2**. `OpenApiConfiguration.java` uses only `io.swagger.v3.oas.annotations.*` — these annotation package names are stable across springdoc 2→3. No source changes needed.

---

## Step 4 — Fix Testcontainers artifact IDs (Testcontainers 2.x)

Spring Boot 4.x pulls in `testcontainers-bom:2.0.x`. In Testcontainers 2.x the artifact IDs were renamed:

**`pom.xml`:**
```xml
<!-- Remove: -->
<artifactId>junit-jupiter</artifactId>
<artifactId>postgresql</artifactId>

<!-- Add: -->
<artifactId>testcontainers-junit-jupiter</artifactId>
<artifactId>testcontainers-postgresql</artifactId>
```

---

## Step 5 — Fix actuator security package moves

`EndpointRequest` and `HealthEndpoint` moved packages in Spring Boot 4.

**`ActuatorSecurityConfiguration.java`:**
```java
// Remove:
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;

// Add:
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
```

---

## Step 6 — Fix MethodSecurityConfiguration (PrePostTemplateDefaults removed)

`PrePostTemplateDefaults` was removed in Spring Security 7 and replaced by `AnnotationTemplateExpressionDefaults`. The `{value}` template placeholder mechanism is unchanged.

**`MethodSecurityConfiguration.java`:**
```java
// Remove:
import org.springframework.security.authorization.method.PrePostTemplateDefaults;

// Replace bean:
@Bean
PrePostTemplateDefaults prePostTemplateDefaults() {
    return new PrePostTemplateDefaults();
}

// With:
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;

@Bean
AnnotationTemplateExpressionDefaults annotationTemplateExpressionDefaults() {
    return new AnnotationTemplateExpressionDefaults();
}
```

---

## Step 7 — Fix WebMvcConfiguration (Jackson 3 package rename)

Spring Boot 4 uses **Jackson 3**, which renamed its entire package root from `com.fasterxml.jackson` to `tools.jackson`. Key impacts:

- `jackson-datatype-jdk8` (`Jdk8Module`) is folded into Jackson 3 core — remove entirely
- `Jackson2ObjectMapperBuilder` is removed — replace with `JsonMapperBuilderCustomizer`
- `JavaTimeModule` is auto-registered by Spring Boot — remove explicit registration
- `JsonMapperBuilderCustomizer` is in `org.springframework.boot.jackson.autoconfigure`

**`WebMvcConfiguration.java`:**
```java
// Remove the old class entirely, replace with:
package com.alimmit.golf.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class WebMvcConfiguration {

  @Bean
  JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
    return builder ->
        builder
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
```

> **Note:** `com.fasterxml.jackson.annotation` and `com.fasterxml.jackson.databind` imports above may also need to change to `tools.jackson.*` equivalents depending on Jackson 3's shim layer — verify at compile time.

---

## Step 8 — Fix @Generated on all entity IDs (Hibernate 7)

**Root cause:** `@Generated(sql = "uuidv7()", writable = true)` triggers Hibernate's internal `GeneratedGeneration` class, whose no-arg constructor was removed in Hibernate 7. The error is `java.lang.NoSuchMethodException: org.hibernate.generator.internal.GeneratedGeneration.<init>()`.

**Fix — all four entity classes** (`ScorecardEntity`, `CourseEntity`, `TeeEntity`, `HandicapEntity`):
```java
// Add import:
import org.hibernate.generator.EventType;

// Replace annotation:
@Generated(sql = "uuidv7()", writable = true)
// With:
@Generated(event = EventType.INSERT)
```

The `columnDefinition = "UUID DEFAULT uuidv7()"` on `@Column` is unchanged — the DB still generates the UUID via the PostgreSQL function. `event = EventType.INSERT` tells Hibernate to re-read the generated value after each INSERT, which is functionally identical to the old behavior. The `writable = true` is not needed since `id` is never set manually before save.

---

## Step 9 — Verify / fix HandicapRepository revision type (conditional)

**File:** `src/main/java/com/alimmit/golf/handicap/HandicapRepository.java`

The repository extends `RevisionRepository<HandicapEntity, UUID, Integer>`. Hibernate Envers 7.x may change the `DefaultRevisionEntity.id` type to `Long`. If the build fails with a type mismatch error on this signature, change the third generic parameter:
```java
// Change Integer → Long if compilation fails:
RevisionRepository<HandicapEntity, UUID, Long>
```

---

## Step 10 — Build and test

```bash
# 1. Compile (catch any type errors early)
./mvnw clean package -DskipTests

# 2. Full test suite (requires Docker for Testcontainers)
./mvnw test

# 3. Format before committing
./mvnw spotless:apply
```

### Expected failure points to watch for:
- **Context startup failure in IT tests** → check `application-test.properties` has the `jwk-set-uri` override
- **`HandicapRepository` / `RevisionRepository` type error** → apply Step 9 fix
- **springdoc classpath conflict** → ensure no transitive springdoc 2.x dependency slips in; run `./mvnw dependency:tree | grep springdoc` to confirm single version
- **Jackson 3 `tools.jackson` package imports** → any remaining `com.fasterxml.jackson` usages in production code may need updating (check entity/DTO annotations)

---

## Critical files

| File | Change |
|---|---|
| `pom.xml` | Boot version bump; remove okta + oauth2-client starters; springdoc version bump; Testcontainers artifact ID rename |
| `src/main/resources/application.properties` | `okta.oauth2.*` → `spring.security.oauth2.resourceserver.jwt.*` |
| `src/test/resources/application-test.properties` | Same rename + add `jwk-set-uri` |
| `ActuatorSecurityConfiguration.java` | Updated `EndpointRequest` + `HealthEndpoint` package paths |
| `MethodSecurityConfiguration.java` | `PrePostTemplateDefaults` → `AnnotationTemplateExpressionDefaults` |
| `WebMvcConfiguration.java` | Remove `Jdk8Module`/`JavaTimeModule`; replace `Jackson2ObjectMapperBuilder` with `JsonMapperBuilderCustomizer` |
| `ScorecardEntity.java`, `CourseEntity.java`, `TeeEntity.java`, `HandicapEntity.java` | `@Generated(sql=..., writable=true)` → `@Generated(event = EventType.INSERT)` + add `EventType` import |
| `src/main/java/com/alimmit/golf/handicap/HandicapRepository.java` | Conditional: `Integer` → `Long` if Hibernate Envers 7 changes default revision ID type |
