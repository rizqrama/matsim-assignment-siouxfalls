# /plan — Plan the Change Before Implementing

Before writing or modifying any code, produce an implementation plan.

## Implementation Plan Template

### Change Type
State whether this is:
- Feature work
- Bug fix
- Refactor
- Test addition
- Documentation update

### Files and Components Affected
List every file, class, module, config, and test that will be:
- Created
- Modified
- Deleted

### Implementation Approach
Describe in plain language how the change will be made:
- What will be added or changed and in what order
- How the new code connects to existing MATSim components
- Any MATSim extension patterns being used
  (e.g. ControlerListener, Guice binding, custom module)

### Tests to Add or Update
List the tests that need to be written or updated as part of
this change. State the level for each:
- Unit test
- Integration test
- Scenario test

### Risks
Identify any risks in the implementation:
- Behavioral risks (could this change simulation outputs?)
- Architectural risks (could this affect other components?)
- Scaling risks (will this still work at 10M+ agents?)

Do not begin implementation until this plan is reviewed.