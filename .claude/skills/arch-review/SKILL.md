---
name: arch-review
description: Perform a holistic architectural review of the Golf API codebase, then file GitHub issues for every finding. Covers application architecture, API design, service boundaries, entity/schema design, query performance, test architecture, security/OWASP Top 10, and DevOps/release. Use this skill whenever the user asks for an architecture review, code review, security review, or says something like "run the arch review" or "/arch-review".
---

# Architectural Review Skill

Performs a structured holistic review of the Golf API and files a GitHub issue for every finding. Findings are never surfaced only as chat output — they go directly into the issue tracker so nothing is lost.

---

## Step 1: Orient — What Has Changed Since the Last Review?

Before exploring the codebase, establish the review scope.

```bash
# Find the most recent arch-review issue to use as a baseline
gh issue list --label "arch-review" --state all --limit 5 --json number,title,createdAt

# Review recent commit activity
git log --oneline -30

# Check currently open issues to avoid filing duplicates
gh issue list --state open --limit 50 --json number,title,labels
```

Note the date of the last review (if any) and the commits since then. Use this to weight the review — focus deeper on areas that have changed.

---

## Step 2: Explore the Codebase

Read enough of the codebase to give accurate findings. Do not rely on memory or prior conversation context — always verify current state. Cover:

### Application Structure
- All entity classes (`*Entity.java`)
- All repository interfaces (`*Repository.java`)
- All service implementations (`*ServiceImpl.java`)
- All controller classes (`*Controller.java`)
- All DTO/record classes
- Flyway migrations (`src/main/resources/db/migration/`)
- `application.properties`

### Configuration & Security
- All files in `config/` package
- `security/` package (meta-annotations)
- `errors/` package (exception handlers)

### Build & Infrastructure
- `pom.xml` (dependencies, plugins, versions)
- `Dockerfile`
- `.github/workflows/*.yml`
- `.github/dependabot.yml`

### Tests
- Test class list (structure, not full content of every file)
- Any abstract test base classes
- Test utility classes (`JwtPersona`, etc.)

Grep for common patterns that signal risk:
```bash
# Hardcoded credentials or secrets
grep -r "password\|secret\|token" src/main/resources/ --include="*.properties"

# Native queries (SQL injection surface)
grep -r "nativeQuery = true" src/main/java/

# String concatenation in queries
grep -r "\"SELECT\|\"UPDATE\|\"DELETE\|\"INSERT" src/main/java/ | grep "+"

# Public service methods that accept arbitrary user IDs
grep -r "String userId\|String golferId\|String createdBy" src/main/java/

# @Transactional missing readOnly on list/get methods
grep -rA2 "public List\|public Optional\|public Page" src/main/java/ | grep -v "readOnly"
```

---

## Step 3: Conduct the Review

Evaluate the codebase against each of the four pillars. For each pillar, produce a list of findings before moving to the next. A finding must have:
- A specific, verifiable problem (not a vague suggestion)
- A file and line reference where possible
- A clear fix

### Pillar 1: Application Architecture

**API Design**
- Are resource URLs consistent? Check for mixed nested/flat patterns.
- Do GET endpoints that return nothing use 404 (not 204)?
- Are list endpoints paginated, or do they return unbounded collections?
- Are HTTP verbs used correctly (POST=create, PATCH=partial update, DELETE=remove)?
- Is there a consistent response structure for errors?

**Service Boundaries**
- Does any service inject a repository from a different domain package?
- Are any cross-user queries exposed on public service interfaces?
- Are event listeners in the correct package (not creating circular domain dependencies)?
- Does every service interface method enforce user scoping?

**Entity & Schema Design**
- Are any JPA repository generic type parameters mismatched with the entity PK type?
- Is there redundant state across fields (e.g., two fields holding the same value from the security context)?
- Are columns that exist in migrations but not in entities causing workaround patterns?
- Does any entity lack an audit trail when it arguably should have one?

**Query Performance**
- Are any list operations unbounded (no LIMIT or Pageable)?
- Do all user-scoped queries have appropriate indexes in migrations?
- Do Envers `_aud` tables have indexes on their entity FK columns?
- Are there O(n) patterns where a bounded query would suffice (e.g., fetching all rows when only the most recent N are needed)?
- Is the async thread pool configured with an explicit queue bound?

### Pillar 2: Test Architecture

**Coverage & Layering**
- Are unit tests (no Spring context) used for pure logic — calculators, mappers, validators?
- Are `@WebMvcTest` tests used for controller layer (not full Spring context)?
- Are Testcontainers IT tests used for repository/integration paths?
- Is there meaningful test coverage on boundary conditions for the handicap calculation table?
- Does the build have a coverage gate (JaCoCo)?

**Test Quality**
- Do smoke tests (`*ApplicationTests`) assert anything beyond context load?
- Are there redundant IT tests that duplicate what controller ITs already cover?
- Are negative/error path tests present (invalid input, missing auth, wrong user)?
- Are async event tests using Awaitility correctly?

### Pillar 3: Security (OWASP Top 10)

Check each OWASP category:

**A01 — Broken Access Control**
- Do all repository queries that return user data include `?#{authentication.name}` or an equivalent user scope filter?
- Are there any service methods that accept an external userId and could be reached from a controller?
- Does every `findById`-style query include an ownership check?

**A02 — Cryptographic Failures**
- Are secrets stored only in environment variables, never in committed files?
- Is JWT validation delegated to the Spring Security OAuth2 resource server (not home-rolled)?

**A03 — Injection**
- Are all JPQL and native queries using named parameters (`:param` or `?#{...}`), never string concatenation?
- Does any full-text search query construct a query string that could be manipulated?
- Is input sanitization applied before building any database query fragments?

**A05 — Security Misconfiguration**
- Is debug-level logging enabled in the default/production application.properties?
- Are security headers configured (HSTS, X-Content-Type-Options, X-Frame-Options)?
- Is CORS explicitly configured?
- Is CSRF explicitly disabled (with a comment) for stateless JWT APIs?
- Is the OpenAPI/Swagger UI accessible without authentication?
- Are GitHub Actions workflow permissions declared at the job level (not workflow level)?

**A06 — Vulnerable Components**
- Is there a Dependabot/Renovate config covering Maven, GitHub Actions, and Docker?
- Are GitHub Actions steps pinned to commit SHAs (not mutable tags)?

**A09 — Security Logging & Monitoring**
- Are authentication failures and authorization denials logged?
- Is there a request correlation ID propagated via MDC?

### Pillar 4: DevOps & Release

**CI Pipeline**
- Does the release workflow run the full test suite before publishing artifacts?
- Is there a code coverage gate in the build?
- Is there a container image vulnerability scan (Trivy, Grype) before image push?
- Is there SAST (SpotBugs + FindSecBugs, or equivalent)?

**Docker & Container**
- Is a multi-stage build used (JDK for build, JRE for runtime)?
- Does the runtime image run as a non-root user?
- Are base image digests pinned?
- Are JVM flags container-aware (`-XX:+UseContainerSupport`)?

**Release Process**
- Is there a documented rollback strategy for failed releases?
- Are database migrations forward-only with no rollback path documented?
- Is semantic versioning applied automatically?

---

## Step 4: Deduplicate Against Open Issues

Before filing anything, cross-reference your findings against the open issues fetched in Step 1.

For each finding:
- If an open issue already covers it → skip it, do not create a duplicate
- If an open issue partially covers it → note the gap but do not re-file the whole thing
- If it is genuinely new → file it

---

## Step 5: File GitHub Issues

For every new finding, create a GitHub issue using this template. **Do not summarize findings in chat — file them directly.**

```bash
gh issue create \
  --title "<type>: <concise description>" \
  --label "<priority-label>,<category-label>" \
  --body "$(cat <<'EOF'
## Background

<2–4 sentences describing the problem, why it matters, and where it exists in the codebase. Include file paths and line references.>

## Acceptance Criteria

- [ ] <specific, verifiable condition 1>
- [ ] <specific, verifiable condition 2>
- [ ] <specific, verifiable condition 3>

## Priority

**<High | Medium | Low>** — <one sentence on why this priority level.>
EOF
)"
```

### Title prefixes
| Prefix | Use for |
|--------|---------|
| `bug:` | Defects, type mismatches, incorrect behavior |
| `security:` | OWASP findings, auth issues, data exposure |
| `performance:` | Unbounded queries, O(n) patterns, missing indexes |
| `architecture:` | Design issues, boundary violations, API design |
| `devops:` | CI/CD, Docker, release pipeline |
| `enhancement:` | Missing features (pagination, coverage gate, etc.) |
| `tech-debt:` | Redundant code, cleanup, conventions |

### Labels to apply
Always apply exactly one priority label and one or more category labels:

**Priority labels:** `priority: high`, `priority: medium`, `priority: low`

**Category labels:** `security`, `performance`, `architecture`, `devops`, `tech-debt`, `bug`, `enhancement`, `github_actions`, `docker`, `java`, `dependencies`

### Priority guide
| Priority | Criteria |
|----------|----------|
| **High** | Data loss, security vulnerability, incorrect behavior in production, or will compound if not addressed soon |
| **Medium** | Design flaw, performance risk, missing safety net — no immediate production impact but will cause pain |
| **Low** | Code clarity, hygiene, nice-to-have safety, low-risk improvements |

---

## Step 6: Apply the arch-review Label and Summarize

After all issues are filed, add the `arch-review` label to each one (create it if it does not exist):

```bash
gh label create "arch-review" --color "C5DEF5" --description "Filed by architectural review" 2>/dev/null || true

# Apply to each issue filed (replace NNN with actual issue numbers)
gh issue edit NNN --add-label "arch-review"
```

Then output a summary table in chat:

```
## Arch Review Complete — <date>

| # | Priority | Title |
|---|----------|-------|
| #NNN | High | ... |
| #NNN | Medium | ... |
| #NNN | Low | ... |

**<N> new issues filed. <M> findings skipped (already tracked).**
```

---

## Important Rules

- **Never surface findings only in chat.** Every finding becomes a GitHub issue or is explicitly skipped as a duplicate.
- **Verify before you assert.** Read the actual current file before claiming something is missing or wrong — do not rely on memory from earlier in the conversation.
- **Be specific.** A finding without a file path and a concrete fix is not a finding — it is a suggestion. Do not file vague issues.
- **Skip if already tracked.** Do not duplicate open issues. Note skipped findings in the summary.
- **Prioritize accurately.** Not everything is high priority. Reserve `priority: high` for things that are broken or actively risky today.
