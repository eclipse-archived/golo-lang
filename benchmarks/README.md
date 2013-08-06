# Golo benchmarks

This Maven project contains a bunch of *micro-benchmarks* for Golo.

They stress some very specific points of the bytecode generation and runtime strategies. The
benchmarks also check how Golo fares against *other* JVM languages with *more or less* similar
constructions.

## Running the benchmarks

This is self-contained Maven project. It uses the awesome
[JUnitBenchmarks](http://labs.carrotsearch.com/junit-benchmarks.html) project.

To run all benchmarks:

    mvn test

To run specific benchmarks:

    mvn test -Dtest=NameOfTest

To disable the awfully slow tests:

    mvn test -DslowTests=false

After *a few minutes* the result shall be available as HTML files. Most web browsers have issues with the JSON data files being served from the local file system, so it is best using a local web server. You can use the provided Ruby script:

    ./http_server.rb

and then open [http://localhost:1981/index](http://localhost:1981/index).

## Important message

You can always make a benchmark tell the story that you want to hear.

Do not try to draw any conclusion on Golo *versus* `(your favorite language)` just based on the
results of a micro-benchmark.

A micro-benchmark is not a real-world application. The most common bottlenecks tend to reveal in
inadequate algorithms and data structures, not even counting the cost of I/O. Another factor in
choosing a language to build all or pieces of an application also include the ecosystem surrounding
a language: libraries, frameworks, etc.

Choosing Golo is a great idea, but do so wisely ;-)

## Important message to fellow JVM language implementers

The test cases try to be as similar as possible across languages. Implementations try to stick to
the idioms of each language.

Some languages support optional types or type annotations. Unsurprisingly, this makes them fare much
better.

**To avoid comparing apples and oranges, we always use such languages without types. We also do not
use language tricks that are known to improve performance.**

We will gladly consider suggestions to improve the benchmark suite as long as fairness is the goal.

We are well-aware that this set of micro-benchmarks is far from being perfect, and that no
definitive conclusions may be drawn from it. In any case, we always focused on a specific point in
Golo, then tried to replicate the same test in other languages. Sometimes the comparison is fair,
sometimes not, but cross-language micro and macro benchmarking is *hard*.

