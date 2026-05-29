---
name: new-task
description: Start a new development task by setting up the correct git branch or worktree, then producing an implementation plan. Use this skill whenever the user says they want to start a new task, story, ticket, issue, feature, fix, or chore — or when they mention a GitHub issue number, or ask to set up a branch or worktree for upcoming work. Trigger even for brief prompts like "start issue 42", "I'm picking up #123", or "let's add X".
---

# New Task Setup Skill

Sets up a git branch or worktree for a task, then either reads the linked GitHub issue or interviews the user to establish clarity, before producing a concrete implementation plan.

---

## Step 1: Check for a GitHub Issue

Ask if there's a linked GitHub issue. If the user provides a number, fetch it:

```bash
gh issue view <NUMBER> --json number,title,body,labels,assignees,comments
```

If `gh` is not available or not authenticated, ask the user to paste the issue title and description directly.

**If there is no issue** (task came from a conversation, ad-hoc request, or the user says there's no ticket) — skip to Step 2b to interview the user directly.

---

## Step 2: Assess Clarity

### With an issue

Read the issue title, body, and any comments. Decide: **is this issue clear enough to act on?**

A clear issue has:
- A well-defined problem or goal
- Enough context to understand scope and affected areas
- Acceptance criteria or a clear definition of done (explicit or obvious)

An unclear issue is missing one or more of those things — vague goals, no reproduction steps for a bug, ambiguous scope, no success criteria.

**Also treat as unclear:** an issue that presents two or more design options without making a decision. Even if acceptance criteria exist, they are conditional on the choice. Always ask before formulating a plan.

**If clear → proceed to Step 3.**
**If unclear → go to Step 2b.**

### Step 2b: Interview for Clarity

Whether there's no issue or the issue is underspecified, ask focused questions to establish:
- What is the goal or problem being solved?
- What is the scope — what's in and what's out?
- What does "done" look like?

Do not ask everything at once — prioritize the 1–3 most important unknowns. Once you have enough clarity, continue to Step 3.

---

## Step 3: Propose Branch Name

Construct a branch name using the convention:

```
<prefix>/<issue-number>-<brief-description>   # when there is a linked issue
<prefix>/<brief-description>                  # when there is no issue
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
- 2–5 words derived from the issue title or task description
- Omit filler words ("a", "the", "for", "to")

**Examples:**
```
feat/42-user-auth-flow
fix/117-login-timeout-error
docs/88-update-api-reference
chore/204-upgrade-eslint-v9
refactor/310-extract-payment-service
chore/brace-style               # no issue — description only
feat/scorecard-export           # no issue — description only
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
git checkout -b <branch-name>
git branch --show-current
```

### Worktree (everything else)

```bash
git checkout main && git pull

git worktree add .worktrees/<branch-name> -b <branch-name>

git worktree list
```

Inform the user of the worktree path.

---

## Step 5: Write an Implementation Plan

Now that context is clear and the workspace is set up, produce a concise implementation plan in this structure:

```
## Implementation Plan — <title>

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

If there is a linked issue, include it in the title: `#<number>: <issue title>`.

Keep steps concrete enough that a developer can act on them without further research. If you need to explore the codebase to make the plan specific, do so using `find`, `grep`, or file reads before writing the plan.

---

## Important Rules

- **Never commit directly to `main`.** Always branch or worktree first.
- Always `git pull` before branching to avoid divergence.
- If the default branch is not `main` (e.g., `master`, `develop`), substitute accordingly.
- Include the issue number in the branch name when one exists — it's the link between code and context.
