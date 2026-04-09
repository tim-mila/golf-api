---
name: new-task
description: Start a new development task from a GitHub issue by setting up the correct git branch or worktree, then producing an implementation plan. Use this skill whenever the user says they want to start a new task, story, ticket, issue, feature, fix, or chore — or when they mention a GitHub issue number, or ask to set up a branch or worktree for upcoming work. Trigger even for brief prompts like "start issue 42" or "I'm picking up #123".
---

# New Task Setup Skill

Sets up a git branch or worktree for a GitHub issue, then reads the issue and either produces a clear implementation plan or interviews the user to clarify an underspecified issue.
 
---

## Step 1: Get the GitHub Issue Number

If the user hasn't provided an issue number, ask for it. Almost all tasks should be tied to a GitHub issue.

The rare exceptions (no issue number needed): purely local chores with no user-facing impact, e.g. "bump a dev dependency", "fix a typo in a comment".

Once you have the issue number, fetch it:

```bash
gh issue view <NUMBER> --json number,title,body,labels,assignees,comments
```

If `gh` is not available or not authenticated, ask the user to paste the issue title and description directly.
 
---

## Step 2: Assess Issue Clarity

Read the issue title, body, and any comments. Decide: **is this issue clear enough to act on?**

A clear issue has:
- A well-defined problem or goal
- Enough context to understand scope and affected areas
- Acceptance criteria or a clear definition of done (explicit or obvious)

An unclear issue is missing one or more of those things — vague goals, no reproduction steps for a bug, ambiguous scope, no success criteria.

**Also treat as unclear:** an issue that presents two or more design options without making a decision. Even if acceptance criteria exist, they are conditional on the choice. Always ask before formulating a plan.

**If clear → proceed to Step 3.**  
**If unclear → go to Step 2b: Interview.**

### Step 2b: Interview for Clarity

Ask focused questions to fill the gaps. Do not ask everything at once — prioritize the 1–3 most important unknowns. Examples:

- "What should the user experience be when X happens?"
- "Is this scoped to Y, or does it need to cover Z as well?"
- "What does 'done' look like — is there a specific output or behavior to verify?"
- "Is there a repro case, or should I look for one first?"

Once you have enough clarity, continue to Step 3.
 
---

## Step 3: Propose Branch Name

Construct a branch name using the convention:

```
<prefix>/<issue-number>-<brief-description>
```

### Prefixes (Conventional Commits)

| Prefix | Use for |
|---|---|
| `feat` | New feature or capability |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `chore` | Tooling, deps, CI, non-production changes |
| `refactor` | Code restructuring without behavior change |
| `test` | Adding or fixing tests |
| `perf` | Performance improvement |
| `style` | Formatting, linting (no logic change) |
| `build` | Build system changes |
| `ci` | CI/CD pipeline changes |

### Description rules
- lowercase, hyphen-separated
- 2–5 words derived from the issue title
- Omit filler words ("a", "the", "for", "to")

**Examples:**
```
feat/42-user-auth-flow
fix/117-login-timeout-error
docs/88-update-api-reference
chore/204-upgrade-eslint-v9
refactor/310-extract-payment-service
```

Propose the name and confirm with the user before creating anything.
 
---

## Step 4: Create Branch or Worktree

**Which to use?**

| Task type | Setup |
|---|---|
| Docs update, small chore, minor config tweak | **Branch** |
| Feature, bug fix, refactor, or any meaningful code change | **Git worktree** |

If in doubt, use a worktree.

### Branch (docs / chores)

```bash
git checkout main && git pull
git checkout -b <prefix>/<issue-number>-<brief-description>
git branch --show-current
```

### Worktree (everything else)

```bash
git checkout main && git pull
 
# Worktree lands in a sibling directory named after the branch slug
git worktree add ../<repo-name>-<issue-number>-<brief-description> \
  -b <prefix>/<issue-number>-<brief-description>
 
git worktree list
```

Inform the user of the worktree path and that they should open that directory for their work.
 
---

## Step 5: Write an Implementation Plan

Now that context is clear and the workspace is set up, produce a concise implementation plan in this structure:

```
## Implementation Plan — #<issue-number>: <issue title>
 
### Goal
One or two sentences on what this change achieves and why.
 
### Approach
Brief description of the strategy (e.g., "Add a middleware layer", "Extend the existing X service", "Replace Y with Z").
 
### Steps
1. ...
2. ...
3. ...
(ordered, actionable, specific — not generic)
 
### Files likely affected
- `path/to/file` — reason
 
### Open questions / risks
- Any remaining unknowns or things to watch for (omit section if none)
```

Keep steps concrete enough that a developer can act on them without further research. If you need to explore the codebase to make the plan specific, do so using `find`, `grep`, or file reads before writing the plan.
 
---

## Important Rules

- **Never commit directly to `main`.** Always branch or worktree first.
- Always `git pull` before branching to avoid divergence.
- If the default branch is not `main` (e.g., `master`, `develop`), substitute accordingly.
- The issue number must appear in every branch name — it's the link between code and context.
 