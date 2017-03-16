# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added
- `catcher` HOF in `gololang.Errors` to handle exceptions ([#417])
- Some warning can printed at runtime:
  - when an imported module is not found
  - when using named arguments with a function that has no named parameters
- `DynamicObject` improvement ([[#423])
- Improvement of the `import` statement:
  - implicit import of the local package ([#446])
  - relative import ([#456])
  - multiple imports ([#457])

### Changed
- Java sources are now compiled with named parameters ([#352], [#429])
- Implicit import of the current module types submodule
- `io` can wrap a reading function to include them in a composition chain
- Misc improvement of the golodoc (index, favicon, style)
- Error messages and command line option helps are now localized ([#441])

### Fixed
- Bug on self referring closures ([#436])


## [3.2.0-M5] - 2016-10-24
### Added
- Support for Java primitive types references (e.g., `long.class`) ([#409])

### Changed
- Upgraded to Gradle 3.1 and other dependencies

### Fixed
- The compiler would hang on unassigned `var` declarations ([#408])

## [3.2.0-M4] - 2016-09-06
### Changed
- Upgrade to Gradle 3.0

### Fixed
- Conflict between property and union attribute resolution ([#399])
- Destructuring assignment inside a closure ([#397])

## [3.2.0-M3] - 2016-07-25
### Added
- A “shebang” command to run Golo programs as shell scripts ([#381])
- OSGi Metadata ([#386])

## [3.2.0-M2] - 2016-06-13
### Added
- Support for unicode escaped chars in strings and chars literals ([#384])
- Golodoc UX improvements ([#382])

## [3.2.0-M1] - 2016-05-02
### Added
- `check` cli command ([#])
- `_` is now allowed in floating numbers and negative exponent with scientific notation ([#375])
- Literal notation for `BigInteger` and `BigDecimal` as well as operator support ([#375])

### Changed
- Dependencies update

## [3.1.0] - 2016-03-21

### Fixed
- gradle and maven templates ([#370])

## [3.1.0-M2] - 2016-03-02
### Changed
- Improve golodoc ([#382])

### Added
- unicode escaped chars in strings and chars literals ([#384])

## [3.1.0-incubation-M1] - 2016-01-14
### Changed
- refactoring of the IR and its creation ([#309])
- implicit imports order ([#311])
- Operator call sites with catchException instead of a guardWithTest ([#327])
- Refactor method and augmentation resolution [(#334])
- Make the orIfNull operator lazy [(#343])

### Added
- Make structures comparable ([#325])
- Allow to create custom factories for structs ([#330])
- Console ANSI codes support ([#314]), ([#336]), ([#337])
- Support Java overloaded instance methods ([#339])
- Add a new library do deal with errors ([#338])

### Fixed
- issues when mixing module state and closures ([#315])
- Lookup augmentations in call stack ([#328])
- Method selection issue ([#333])
- Check for duplicated types compile time ([#345])
- Named arguments and anonymous calls ([#346])

## [3.0.0-incubation-M3] - 2015-10-19
### Changed
- Refactoring from `fr.insalyon.citi.golo` to `org.eclipse.golo packages`.
- FindBugs integration in the build process.
- Fixes have been applied to address the relevant FindBugs reports, and unappropriated ones have been silenced.
- Parser error traces have been simplified to avoid confusion.
- Misc fixes.


## [3.0.0-incubation-M2] - 2015-09-07
### Added
- Collection comprehensions
- Union types now have special methods synthesized from members to facilitate tests.

### Changed
- The documentation is now generated with AsciiDoctor instead of AsciiDoc.
- The build has been migrated from Maven to Gradle.
- The new `box(obj)` predefined function boxes an object reference into a `java.util.concurrent.atomic.AtomicReference`. This is useful to inject mutable state into closures.

### Fixed
- reversed_range has been renamed to reversedRange for consistency reasons.


## [3.0.0-incubation-M1] - 2015-07-27
### Added
- Support for Java 8 lambdas / functional interfaces.
- Support for named arguments, leveraging JDK 8 bytecode metadata.
- New union types.
- Augmentations can now define a fallback method.
- New lazy lists.
- `foreach` loops can now have a guard condition.
- Easier adapters generation API.
- The `fun` function can now retrieve instance methods.

### Changed
- JDK 8 is now required.
- Function references are now represented using FunctionReference rather than straight MethodHandle.
- Decorators can now change function arities.
- Deprecated array methods and functions have been removed.
- CLI subcommands are now defined through a SPI for modularity and extensibility.
- Documentation generation is now self-contained with Asciidoctor instead of Asciidoc.

### Fixed
- Operators priorities and left-associativity have been fixed in the LL(k) parser.
- Fixes in arithmetic comparison operators.
- Fixes in special character escaping in String and char literals.


## [v2.1.0] - 2015-03-17
### Added
- New methods on collection-like objects: `head`, `tail` and `isEmpty`
- Direct call of anonymous functions allowed
- Tuples are comparable
- Module names escaping in `golo new`


## [v2.0.0] - 2015-01-20
### Added
- Full Unicode support: names (variables, functions) can now contain (almost) any characters (including emoji). All files are considered UTF-8
- Named Augmentations: augmentations can have a name to be latter applied
- Banged function call
- Literal range values and decreasing range
- Number type conversion functions
- New standard library functions

## Change
- Improved golodoc
- Improved checks by the compiler


## [v1.1.0] - 2014-09-21
## Added
- Python-style decorators
- Command-line improvement: support for folders
- Ctag generation


[Unreleased]: https://github.com/eclipse/golo-lang/compare/milestone/3.2.0-M5...HEAD
[3.2.0-M5]: https://github.com/eclipse/golo-lang/compare/milestone/3.2.0-M4...milestone/3.2.0-M5
[3.2.0-M4]: https://github.com/eclipse/golo-lang/compare/milestone/3.2.0-M3...milestone/3.2.0-M4
[3.2.0-M3]: https://github.com/eclipse/golo-lang/compare/milestone/3.2.0-M2...milestone/3.2.0-M3
[3.2.0-M2]: https://github.com/eclipse/golo-lang/compare/milestone/3.2.0-M1...milestone/3.2.0-M2
[3.2.0-M1]: https://github.com/eclipse/golo-lang/compare/release/3.1.0...milestone/3.2.0-M1
[3.1.0]: https://github.com/eclipse/golo-lang/compare/milestone/3.1.0-M2...release/3.1.0
[3.1.0-M2]: https://github.com/eclipse/golo-lang/compare/milestone/3.1.0-incubation-M1...milestone/3.1.0-M2
[3.1.0-incubation-M1]: https://github.com/eclipse/golo-lang/compare/release/3.0.0-incubation...milestone/3.1.0-incubation-M1
[3.0.0-incubation]: https://github.com/eclipse/golo-lang/compare/milestone/3.0.0-incubation-M3...milestone/3.0.0-incubation
[3.0.0-incubation-M3]: https://github.com/eclipse/golo-lang/compare/milestone/3.0.0-incubation-M2...milestone/3.0.0-incubation-M3
[3.0.0-incubation-M2]: https://github.com/eclipse/golo-lang/compare/milestone/3.0.0-incubation-M1...milestone/3.0.0-incubation-M2
[3.0.0-incubation-M1]: https://github.com/eclipse/golo-lang/compare/eclipse_initial...milestone/3.0.0-incubation-M1
[v2.1.0]: https://github.com/eclipse/golo-lang/compare/_old/v2.0.0..._old/v2.1.0
[v2.0.0]: https://github.com/eclipse/golo-lang/compare/_old/v1.1.0..._old/v2.0.0
[v1.1.0]: https://github.com/eclipse/golo-lang/compare/_old/v1.0.0..._old/v1.1.0
[v1.0.0]: https://github.com/eclipse/golo-lang/compare/_old/v0-preview12..._old/v1.0.0
[#309]: https://github.com/eclipse/golo-lang/pull/309
[#311]: https://github.com/eclipse/golo-lang/pull/311
[#314]: https://github.com/eclipse/golo-lang/pull/314
[#315]: https://github.com/eclipse/golo-lang/pull/315
[#325]: https://github.com/eclipse/golo-lang/pull/325
[#327]: https://github.com/eclipse/golo-lang/pull/327
[#328]: https://github.com/eclipse/golo-lang/pull/328
[#330]: https://github.com/eclipse/golo-lang/pull/330
[#333]: https://github.com/eclipse/golo-lang/pull/333
[#334]: https://github.com/eclipse/golo-lang/pull/334
[#336]: https://github.com/eclipse/golo-lang/pull/336
[#337]: https://github.com/eclipse/golo-lang/pull/337
[#338]: https://github.com/eclipse/golo-lang/pull/338
[#339]: https://github.com/eclipse/golo-lang/pull/339
[#343]: https://github.com/eclipse/golo-lang/pull/343
[#345]: https://github.com/eclipse/golo-lang/pull/345
[#346]: https://github.com/eclipse/golo-lang/pull/346
[#352]: https://github.com/eclipse/golo-lang/pull/352
[#370]: https://github.com/eclipse/golo-lang/pull/370
[#375]: https://github.com/eclipse/golo-lang/pull/375
[#384]: https://github.com/eclipse/golo-lang/pull/384
[#386]: https://github.com/eclipse/golo-lang/pull/386
[#381]: https://github.com/eclipse/golo-lang/pull/381
[#382]: https://github.com/eclipse/golo-lang/pull/382
[#384]: https://github.com/eclipse/golo-lang/pull/384
[#397]: https://github.com/eclipse/golo-lang/pull/397
[#399]: https://github.com/eclipse/golo-lang/pull/399
[#408]: https://github.com/eclipse/golo-lang/pull/408
[#409]: https://github.com/eclipse/golo-lang/pull/409
[#417]: https://github.com/eclipse/golo-lang/pull/417
[#423]: https://github.com/eclipse/golo-lang/pull/423
[#429]: https://github.com/eclipse/golo-lang/pull/429
[#436]: https://github.com/eclipse/golo-lang/pull/436
[#441]: https://github.com/eclipse/golo-lang/pull/441
[#446]: https://github.com/eclipse/golo-lang/pull/446
[#456]: https://github.com/eclipse/golo-lang/pull/456
[#457]: https://github.com/eclipse/golo-lang/pull/457

