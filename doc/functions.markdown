# Functions

Functions are first-class citizen in Golo. Here is how to define and call some.

## Parameter-less functions

Golo modules can define functions as follows:

```golo
module sample

function hello = {
  return "Hello!"
}
```

In turn, you may invoke a function with a familiar notation:

```golo
let str = hello()
```

A function needs to return a value using the `return` keyword. Some languages state that the last
statement is the return value, but Golo does not follow that trend. We believe that `return` is more
explicit, and that a few keystrokes in favour of readability is still a good deal.

Still, you may omit `return` statements if your function does not return a value:

```golo
function printer = { 
  println("Hey!")
}
```

If you do so, the function will actually return `null`, hence `result` in the next statement is
`null`:

```golo
# result will be null
let result = printer()
```

## Functions with parameters

Of course functions may take some parameters, as in:

```golo
function addition = |a, b| {
  return a + b
}
```

Invoking functions that take parameters is straightforward, too:

```golo
let three = addition(1, 2)
let hello_world = addition("hello ", "world!")
```

## Variable-arity functions

Functions may take a varying number of parameters. To define one, just add `...` to the last
parameter name:

```golo
function foo = |a, b, c...| {
  # ...
}
```

Here, `c` catches the variable arguments in an array, just like it would be the case with Java. You
can thus treat `c` as being a Java object of type `Object[]`.

Calling variable-arity functions does not requiring wrapping the last arguments in an array. While
invoking the `foo` function above, the following examples are legit:

```golo
# a=1, b=2, c=[]
foo(1, 2)

# a=1, b=2, c=[3]
foo(1, 2, 3)

# a=1, b=2, c=[3,4]
foo(1, 2, 3, 4)
```

Because the parameter that catches the last arguments is an array, you may call the array functions.
Given:

```golo
function elementAt = |index, args...| {
  return aget(args, index)
}
```

then:

```golo
# prints "2"
println(elementAt(1, 1, 2, 3))
```

## Functions from other modules

Suppose that we have a module `Foo.Bar`:

```golo
module Foo.Bar

function f = {
  return "f()"
}
```

We can invoke `f` from another module by prefixing it with its module name:

```golo
let r = Foo.Bar.f()
```

Of course, we may also take advantage of an `import` statement:

```golo
module Somewhere.Else

import Foo.Bar

function plop = {
  return f()
}
```

## Local functions

By default, functions are visible outside of their module. You may restrict the visibility of
a function by using the `local` keyword:

```golo
module Foo

local function a = {
  return 666
}

function b = {
  return a()
}
```

Here, `b` is visible while `a` can only be invoked from within the `Foo` module. Given another
module called `Bogus`, the following would fail at runtime:

```golo
module Bogus

function i_will_crash = {
  return Foo.b()
}
```

