# Basics

## Hello world

Golo source code need to be placed in *modules*. Module names are separated with dots, as in:

    Foo
    foo.Bar
    foo.bar.Baz
    (...)

It is suggested yet not enforced that the first elements in a module name are in lowercase, and
that the last one have an uppercase first letter.

A Golo module can be executable if it has a *function* named `main` and that takes an argument
for the JVM program arguments:

```golo
module hello.World

function main = |args| {
  println("Hello world!")
}
```

`println` is a predefined function that outputs a value to the standard console. As you can
easily guess, here we output `Hello, world!` and that is an awesome achievement.

ADD HOW TO COMPILE / INVOKE

## Variables and constants

Golo does not check for types at compile time, and they are not declared. Everything happens at
runtime in Golo.

Variables are declared using the `var` keyword, while constants are declared with `let`. It is
strongly advised that you favour `let` over `var` unless you are certain that you need mutability.

Variables and constants need to be initialized when declared. Failing to do so results in
a compilation error.

Here are a few examples:

```golo
# Ok
var i = 3
i = i + 1

# The assignment fails because truth is a constant
let truth = 42
truth = 666

# Invalid statement, variables / constants have to be initialized
var foo
```


