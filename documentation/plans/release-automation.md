# Release Automation Plan — Trunk-Based Development

## Context
Every push to `main` produces a versioned release: semver computed from conventional commits, a git tag on the real commit, a Docker image pushed to GHCR, and a GitHub Release with changelog. Tests are not re-run (already validated on PR via `test.yml`).

---

## Design decisions

**Tag = version.** `pom.xml` is never updated by CI. The Docker image is tagged with the semver version by the workflow — the JAR name inside the container is irrelevant to deployment. This avoids bot commits, branch protection bypasses, and `[skip ci]` loop-prevention hacks.

**Every push to main releases.** The tag action runs unconditionally on push to `main`. `chore:`, `refactor:`, etc. produce a patch bump via `default_bump: patch`.

---

## Files changed

| File | Change |
|------|--------|
| `.github/workflows/release.yml` | Created |
| `Dockerfile` | Fixed hardcoded JAR version on line 15 → glob |

---

## Workflow (6 steps)

```
push to main
  → checkout (full history)
  → mathieudutour/github-tag-action: compute semver + create tag on HEAD
  → docker login to GHCR
  → docker buildx setup
  → build + push image (ghcr.io/<owner>/golf-api:<version> + :latest, GHA layer cache)
  → softprops/action-gh-release: create release with changelog
```

## Semver bump rules

| Commit prefix | Bump |
|---------------|------|
| `feat:` | minor |
| `fix:` | patch |
| everything else | patch (`default_bump: patch`) |
| `BREAKING CHANGE` footer | major |

---

## GitHub repo settings prerequisite

**Settings → Actions → General → Workflow permissions** → set to **"Read and write permissions"**

This is the only prerequisite. No branch protection bypass needed.

---

## First release version

No existing `v*` tags → base `0.0.0`. Most recent commit is `feat: spring boot 4 upgrade` → first release is **`v0.1.0`**. To start at `v1.0.0` instead: push tag `v0.9.9` manually before merging.

---

## TODO / Future improvements

- **Maven CI-friendly versions** — update `pom.xml` to use `<version>${revision}</version>` with a default of `0.0.1-SNAPSHOT`, and pass `-Drevision=<version>` via a Docker build arg in the workflow. Produces a correctly versioned JAR inside the container without requiring bot commits to main. See: https://maven.apache.org/maven-ci-friendly.html

---

## Docker image location

`ghcr.io/<owner>/golf-api` — visible under the repository's Packages sidebar. Visibility inherits from the repo (private repo → private image). Pull requires a PAT or deploy key with `read:packages` scope.
