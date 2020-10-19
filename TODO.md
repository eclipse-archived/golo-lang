# Reimplementing destructuring

branch: feat/destruct-reboot

Trying to reimplement the destructuring feature better (see #524 for a full introduction to issues of current implementation).

## TODO

- [x] implement the new desugarization
  - [x] allows to explicitly skip the rest (e.g. with special arg `_`) to avoid generating an unused (recursive) member
- [x] Adds a custom exception to signal invalid destructuring
- update the known `destruct()` method in the stdlib
  - provides a new destructuring method
    - [x] Tuple
    - [x] struct
    - [x] array
    - [x] union
    - [x] iter/collections
    - [x] Result
    - [x] map item
    - [x] lazy lists
    - [x] ranges
    - [x] When IR
  - [x] mark the old one deprecated (may depend on PR #551 to annotate golo code)
    - [x] use the real deprecated macro when #551 is merged
- [ ] ~~provides augmentations to adapt both ways ?~~
- [ ] update the doc
  - [ ] describe new style destruct
  - [ ] rationale
  - [ ] document feature flag

