# /checkpoint — Close with Commit and Push

The current task is complete. Close it with a formal checkpoint.

## Checkpoint Checklist

Before committing, verify all Quality Gates are met:

- [ ] Technical specification was written before any code
- [ ] Ambiguities were clarified or recorded as assumptions
- [ ] Key concepts were explained before implementation
- [ ] Scenario or code runs successfully
- [ ] Behavior is explainable, not just "working"
- [ ] No hardcoded parameters
- [ ] Config fully drives execution
- [ ] Tests cover key logic
- [ ] Code follows MATSim extension patterns
- [ ] Inline comments are beginner-friendly
- [ ] Handover log entry written in
  docs/<scenario-name>-development-log.md

## Commit Prompt

Suggest a checkpoint commit using this format:

**Commit scope**: list the files and components included
in this commit

**Suggested commit message**:
```
<Short imperative summary of what was done>

- <Key change 1>
- <Key change 2>
- <Key change 3>

<Optional: note any known limitations or follow-up items>
```

## Push Prompt

After committing, prompt the user to push changes to the
remote repository:

"Please push these changes to your remote repository to ensure
your work is safely stored and accessible to collaborators."

```
git push
```

This is a formal development milestone. Do not skip the push.