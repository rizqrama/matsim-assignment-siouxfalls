# /spec — Trigger Specification Workflow

Before writing any code or making any change, produce a full
technical specification for the current task.

Work through these three steps in order:

## Step 1: Capture Command
- Restate the user's request in your own words
- Extract both explicit requirements (what was stated) and
  implied requirements (what is needed but not said)

## Step 2: Write Specification
Produce a complete technical specification covering:
- **Objective**: what this task is trying to achieve
- **Scope**: what is included and what is explicitly excluded
- **Assumptions**: anything not stated that you are treating as true
- **Affected components**: files, classes, configs, tests, outputs
- **Expected outputs**: what the completed task should produce
- **Acceptance criteria**: how success will be verified
- **Risks**: behavioral, architectural, or scaling concerns
- **Dependencies**: what must exist or be true for this to work
- **Edge cases**: what could go wrong that is not obvious

## Step 3: Clarify
- List any ambiguities, contradictions, or underspecified requirements
- Ask follow-up questions before proceeding
- If proceeding with assumptions, state each one explicitly

Do not write any code until the specification is confirmed.