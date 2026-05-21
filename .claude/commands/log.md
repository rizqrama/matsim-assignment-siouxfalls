# /log — Write Handover Log Entry

Write a dated entry for the current task in the correct scenario
handover log file at:

docs/<scenario-name>-development-log.md

Use the following structure exactly. Every field is mandatory
unless explicitly marked optional.

---

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
- How success is verified

### Clarifications / Assumptions
Numbered list of any ambiguities resolved and assumptions made.

### Decision Taken
What was actually implemented and how.

### Reason for Decision
Why this approach was chosen over alternatives.

### Alternatives Considered
What other approaches were evaluated and why they were not chosen.

### Tests Added / Updated
List tests added or updated. If none, state why and describe
what manual validation was performed instead.

### Known Limitations
Numbered list of known gaps, incomplete items, or deferred work.

### Next Recommended Checkpoint
- Suggested commit scope
- Suggested commit message

---

After writing the entry, prompt the user to review it before
committing. Then proceed to /checkpoint.