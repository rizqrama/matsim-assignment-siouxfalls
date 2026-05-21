# CLAUDE.md — Documentation Context (docs/)

> This file is loaded automatically when working inside the docs/ directory.
> It extends the root CLAUDE.md with rules specific to writing and
> maintaining the project handover log.

---

## Documentation Principle

Documentation is part of development, not an afterthought.

Every meaningful development action must be traceable. The handover log
must be complete enough for another developer to understand:
- what was done
- why it was done
- what remains
- how to continue safely

---

## Handover Log Location and Naming

Every scenario has its own dedicated log file:

```
docs/<scenario-name>-development-log.md
```

Examples:
- `docs/higashi-hiroshima-development-log.md`
- `docs/test-scenario-development-log.md`

Never consolidate multiple scenario logs into one file.
Never leave development decisions recorded only in chat.

---

## Step 8: Document As You Go

When updating the handover log, write a dated entry using this
exact structure. Every field is mandatory unless explicitly marked
optional.

```markdown
## YYYY-MM-DD: <Short Task Title>

### Task
One or two sentences describing what was requested.

### Technical Specification

**Objective**: What this task is trying to achieve.

**Scope**:
- What is included
- What is explicitly excluded

**Affected Components**:
- List of files, classes, configs, and outputs touched

**Expected Outputs**:
- What the completed task should produce

**Acceptance Criteria**:
- How success will be verified

### Clarifications / Assumptions
Numbered list of any ambiguities resolved and assumptions made.

### Decision Taken
What was actually implemented and how.

### Reason for Decision
Why this approach was chosen over alternatives.

### Alternatives Considered
What other approaches were evaluated and why they were not chosen.

### Tests Added / Updated
List tests added or updated. If none, state why and what manual
validation was performed instead.

### Known Limitations
Numbered list of known gaps, incomplete items, or things deferred.

### Next Recommended Checkpoint
- Suggested commit scope
- Suggested commit message
```

---

## Documentation Quality Gates

Before any documentation task is considered complete:

- [ ] Entry is dated (YYYY-MM-DD format)
- [ ] Task description is clear and unambiguous
- [ ] Technical specification is complete
- [ ] All assumptions are explicitly recorded
- [ ] Decision and rationale are recorded
- [ ] Alternatives considered are noted
- [ ] All affected files and components are listed
- [ ] Tests added or updated are listed (or absence explained)
- [ ] Known limitations are recorded
- [ ] Next recommended checkpoint is stated
- [ ] Entry is written in the correct log file for this scenario
- [ ] User is prompted for a checkpoint commit
- [ ] User is prompted to push changes to the remote repository

---

## Documentation Anti-Patterns

- Writing a log entry after the fact from memory instead of
  during development
- Recording decisions only in chat and not in the log file
- Omitting the "reason for decision" field
- Omitting alternatives considered
- Leaving known limitations blank when limitations exist
- Using vague task titles that do not identify what was done