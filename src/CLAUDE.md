# CLAUDE.md — Source Code Context (src/)

> This file is loaded automatically when working inside the src/ directory.
> It extends the root CLAUDE.md with rules specific to writing,
> modifying, and testing Java/MATSim code.

---

## Operating Mode (For Code Tasks)

Follow this mandatory sequence for every code-related request:

### 1. CAPTURE COMMAND
- Read the request carefully
- Extract explicit and implied requirements

### 2. WRITE SPECIFICATION (before any code)
- Restate the task as a precise technical specification
- Define:
    - objective
    - scope
    - assumptions
    - affected components
    - expected outputs
    - acceptance criteria
- Identify risks, dependencies, and likely edge cases

### 3. CLARIFY AMBIGUITIES
- Surface any ambiguity, contradiction, or underspecified requirement
- Do not silently guess when requirements materially affect
  architecture, behavior, or outputs
- State any reasonable assumptions explicitly

### 4. TEACH BEFORE BUILDING
- Before writing any code or config, explain:
    - what MATSim component is being used and what it does
    - why this approach is chosen over alternatives
    - how it connects to the real-world transport concept behind it
    - what best practice this follows and why it matters for
      larger or more complex projects down the line
- If the user is setting something up for the first time, also
  explain how this piece fits into the overall MATSim project
  structure — treat it as a guided walkthrough, not just a
  code delivery
- Use analogies where helpful

### 5. PLAN THE CHANGE
- Identify affected files, modules, configs, and tests
- State whether the change is:
  feature work / bug fix / refactor / test addition / documentation update

### 6. IMPLEMENT SYSTEMATICALLY
- Write code only after the specification is explicit
- Keep changes minimal, traceable, and aligned with MATSim
  extension patterns
- Always include inline comments written for a beginner audience

### 7. TEST
- Add or update tests at the appropriate level:
  unit, integration, or scenario
- Verify behavior against specification, not just that the
  code runs without errors
- For every bug fix, add a regression test

---

## Thinking Model

- Always ask: "What real-world transport behavior is this model
  trying to represent?"
- Reduce problems to: agents, choices, constraints, feedback loops
- Prefer observing dynamics (events) over inspecting static plans
- Assume scale from the start (millions of agents), even when
  prototyping small

---

## MATSim-First Principles

- Always use MATSim APIs before considering external tools
- Extend via: ControlerListeners, custom modules, Guice bindings
- Never modify MATSim core unless absolutely necessary
- Never duplicate MATSim structures (Population, Network, Facilities)
- Treat Events as the primary source of truth for analysis
- Prefer behavioral realism over calibration shortcuts
- Config must fully drive execution — no hardcoded parameters

---

## Testing Strategy

- Validate behavior, not just code
- Focus on:
    - config correctness
    - scenario integrity
    - event generation
    - Controler execution
    - conformance to specification

### Levels
- Unit tests → validate isolated logic
- Integration tests → validate full pipelines
- Scenario tests → run small simulations to verify
  end-to-end behavior before scaling up

### Rule
Every bug fix must include a regression test — a test that would
have caught the bug before it was fixed, and will catch it again
if it returns.

---

## Real-World Constraints

- Simulations must be containerizable (e.g. Docker)
- Must run in batch environments (e.g. AWS Batch / Fargate)
- Pipelines include: OSM → network, GTFS → transit schedule,
  demand generation
- Outputs must be reproducible, incrementally storable, and
  documented for handover
- Design for 10M+ agents by default

---

## Quality Gates (Code Tasks)

Before any code task is considered complete:

- [ ] Technical specification written first
- [ ] Ambiguities clarified or explicitly recorded as assumptions
- [ ] Key concepts and best practices explained before implementation
- [ ] How this fits into the broader MATSim project structure
  explained if relevant
- [ ] Scenario runs successfully
- [ ] Behavior is explainable, not just "working"
- [ ] No hardcoded parameters
- [ ] Config fully drives execution
- [ ] Tests cover key logic
- [ ] Code follows MATSim extension patterns
- [ ] Inline comments are beginner-friendly
- [ ] User is prompted for a checkpoint commit
- [ ] User is prompted to push changes to the remote repository