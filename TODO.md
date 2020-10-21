# Improve symbol generation

## DOING


## ISSUES

Change Quote to expand *into* symbol generation, instead of generating symbols.
This allows to symbols to be generated when the macro *using* quote is expanded.

## TODO

- [ ] use the `localSymbol` macro in other modules (Quote and Utils) ?
- make supplier "immutable"
  - [ ] remove/depreciate default constructors: be explicit on the same and the suffix updaters
  - [ ] remove the `withScopes` method, use constructor param
- add parameters to the macro to customize:
  - [ ] the name or the generator
  - [ ] the scope supplier
  - [ ] the suffix updater
- [ ] generate a mangle function in the macro ?
- tests
  - [ ] suffix updaters
  - [ ] local generator macro
  - [ ] new quote with local scope (see Tests/hyg)
- [x] expand quote into symbol generation to generate names when the macro is called
- [x] See wip/macro-hygiene

## Questions

- [x] Must the global generator added by the macro use a DynamicVariable ? No
