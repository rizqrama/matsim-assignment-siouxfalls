# /test — Verify Behavior Against Specification

Run through the testing checklist for the current task.

## Testing Checklist

### 1. Identify Test Level
Determine which level(s) of testing apply to this change:
- **Unit test** → validates isolated logic in a single class or method
- **Integration test** → validates a full pipeline or multi-component flow
- **Scenario test** → runs a small simulation to verify end-to-end behavior

### 2. Write or Update Tests
- Tests must validate behavior, not just that code runs without errors
- Each test should correspond to a specific acceptance criterion
  from the task specification
- Tests must be runnable independently and reproducibly

### 3. Regression Rule
If this task is a bug fix:
- Write a regression test that would have caught the bug
  before it was fixed
- Confirm the test fails without the fix and passes with it

### 4. Validation Checklist
Before marking testing complete:

- [ ] Tests written at the appropriate level
- [ ] Tests validate behavior against specification
- [ ] All tests pass
- [ ] Bug fix includes a regression test (if applicable)
- [ ] Test names clearly describe what behavior is being verified
- [ ] No hardcoded test data that would break on environment change

### 5. If Automated Tests Are Not Feasible
State explicitly:
- Why automated tests were not added
- What manual validation was performed instead
- What observable evidence confirms correct behavior