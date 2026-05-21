# CLAUDE.md — Scenario Context (scenarios/)

> This file is loaded automatically when working inside the scenarios/
> directory. It extends the root CLAUDE.md with rules specific to
> building, modifying, and validating MATSim scenario inputs and outputs.

---

## Scenario Thinking Model

Before touching any scenario file, always ask:
- What real-world transport behavior is this scenario trying to represent?
- Which agents are involved, what choices do they make, what constraints
  apply, and what feedback loops exist?
- Is this output reproducible from the inputs alone?

Reduce every scenario problem to:
- agents
- choices
- constraints
- feedback loops

Prefer observing dynamics (events) over inspecting static plans.
Assume scale from the start — even when prototyping with 1% samples,
design as if the full scenario will run 10M+ agents.

---

## Real-World Constraints

- All scenario pipelines must be containerizable (e.g. Docker)
- Must run reproducibly in batch environments (e.g. AWS Batch / Fargate)
- Standard pipeline stages:
    - OSM → network conversion and cleaning
    - GTFS → transit schedule preparation
    - Demand generation from OD matrices or synthetic population
    - Simulation execution
    - Output analysis
- Outputs must be:
    - reproducible from the same inputs
    - incrementally storable (do not overwrite intermediate artifacts)
    - documented in the scenario handover log

---

## Scenario Specification Requirements

When specifying any scenario task, always define:

- **Input files**: exact paths and formats
- **Output files**: exact paths and formats
- **Coordinate system**: always state the CRS explicitly
  (e.g. EPSG:2445 for Higashi-Hiroshima)
- **Scale**: which sample size is being produced (1%, 10%, 100%)
- **Mode coverage**: which transport modes are included
- **Spatial scope**: which zones or network area is covered
- **Reproducibility mechanism**: how the same output can be regenerated

---

## Scaling Rules

- Always test with a small sample (1% or small synthetic population)
  before running at full scale
- Never run a full-scale simulation without a passing small-scale
  validation first
- Design for 10M+ agents by default even when current scenario is smaller
- Expect and plan for:
    - memory constraints on large networks
    - long runtimes on full populations
    - large event streams that cannot be loaded entirely into memory

---

## Scenario Output Quality Gates

Before any scenario task is considered complete:

- [ ] Input files are explicitly identified and paths confirmed
- [ ] Output files are written to the correct scenario directory
- [ ] Coordinate system is explicitly stated and consistent
- [ ] Small-scale validation run completed before full scale
- [ ] Simulation runs successfully end-to-end
- [ ] Behavior is explainable — outputs are consistent with
  the real-world transport behavior being modeled
- [ ] Outputs are reproducible from the same inputs
- [ ] No hardcoded parameters — config fully drives execution
- [ ] Scenario handover log updated with this task's entry
- [ ] User is prompted for a checkpoint commit
- [ ] User is prompted to push changes to the remote repository

---

## Scenario Anti-Patterns

- Running full-scale simulations without small-scale validation
- Overwriting raw input artifacts with processed outputs
- Using hardcoded file paths instead of config-driven paths
- Mixing preprocessing, simulation execution, and analysis
  in the same class or script
- Generating outputs that cannot be reproduced from the same inputs
- Leaving the coordinate system undocumented or inconsistent
  across scenario files