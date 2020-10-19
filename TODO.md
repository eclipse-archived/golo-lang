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
    - [x] Binary operation IR
    - [x] NamedArgument
  - [x] mark the old one deprecated (may depend on PR #551 to annotate golo code)
    - ~~use the real deprecated macro when #551 is merged~~ (not possible, compilation dependency)
- ~~provides augmentations to adapt both ways ?~~ (not needed)
- [x] Adds a feature flag using property / macro to switch old/new style
- update the doc
  - [x] describe new style destruct
  - [x] document feature flag

