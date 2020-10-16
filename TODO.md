# Reimplementing destructuring

branch: feat/destruct-reboot

Trying to reimplement the destructuring feature better (see #524 for a full introduction to issues of current implementation).

## TODO

- [ ] implement the new desugarization
- [ ] update the known `destruct()` method in the stdlib
  - [ ] provides a new destructuring method
  - [ ] mark the old one deprecated (may depend on PR #551 to annotated golo code)
- [ ] provides augmentations to adapt both ways
