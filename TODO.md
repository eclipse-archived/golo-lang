# Reimplementing destructuring

branch: feat/destruct-reboot

Trying to reimplement the destructuring feature better (see #524 for a full introduction to issues of current implementation).

## TODO

- [x] implement the new desugarization
  - [ ] allows to explicitly skip the rest (e.g. with special arg `_`) to avoid generating an unused (recursive) member
- [x] Adds a custom exception to signal invalid destructuring
- update the known `destruct()` method in the stdlib
  - provides a new destructuring method
    - [x] Tuple
    - [x] struct
    - [x] array
    - [x] union
    - [ ] iter/collections
    - [x] Result
    - [x] map item
    - [x] lazy lists
    - [x] ranges
    - [x] When IR
  - [ ] mark the old one deprecated (may depend on PR #551 to annotated golo code)
  - [ ] if applicable, use the new one in the old one
- [ ] provides augmentations to adapt both ways
- [ ] update the doc

