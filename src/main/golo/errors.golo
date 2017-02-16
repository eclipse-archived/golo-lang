# ............................................................................................... #
#
# Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# ............................................................................................... #

----
Useful augmentations, decorators and function to deal with errors in Golo.
----
module gololang.Errors

import java.util.`function
import java.util.Collections

# ............................................................................ #
## Constructors

----
Constructor for `gololang.error.Result` values.

- *param* `value`: the value to encapsulate in the `Result`
----
function Ok = |value| -> gololang.error.Result.ok(value)

----
Constructor for `gololang.error.Result` errors.

- *param* `message`: the message for the error in the `Result`
----
function Error = |message| -> gololang.error.Result.fail(message)

----
Constructor for an empty `gololang.error.Result`.
----
function Empty = -> gololang.error.Result.empty()

----
Constructor for an empty `java.util.Optional`.
----
function None = -> java.util.Optional.empty()

----
Constructor `java.util.Optional` values.

- *param* `value`: the value to encapsulate in the `Optional`
----
function Some = |value| -> java.util.Optional.of(value)

----
“Smart” constructor for `java.util.Optional`.

If the given value is already an `Optional`, returns it unchanged; if it's a
`Result`, converts it into an `Optional`, and create an `Optional` otherwise
(using `ofNullable`)
----
function Option = |v| -> match {
  when v oftype java.util.Optional.class then v
  when v oftype gololang.error.Result.class then v: toOptional()
  otherwise java.util.Optional.ofNullable(v)
}

----
“Smart” constructor for `gololang.error.Result`.

If the given value is already a `Result`, returns it unchanged
and create an `Optional` otherwise.
----
function Result = |v| -> match {
  when v oftype gololang.error.Result.class then v
  otherwise gololang.error.Result.of(v)
}

# ............................................................................ #
augment java.util.Optional {
  ----
  Test if this optional is empty.
  ----
  function isEmpty = |this| -> not this: isPresent()

  ----
  Alias for `isEmpty`
  ----
  function isNone = |this| -> not this: isPresent()

  ----
  Alias for `isPresent`
  ----
  function isSome = |this| -> this: isPresent()

  ----
  Test if this optional contains a value equals to the provided one.
  ----
  function isSome = |this, value| ->
      this: isPresent() and this: get(): equals(value)

  function iterator = |this| -> match {
    when this: isPresent() then singleton(this: get()): iterator()
    otherwise emptyIterator()
  }

  ----
  Reduce this option using `func` with `init` as initial value.

  For instance:

      Some("b"): reduce("a", |x, y| -> x + y) == "ab"
      None(): reduce(42, |x, y| -> x + y) == 42

  - *param* `init` the initial value
  - *param* `func` the aggregation function
  - *return* the initial value if this is empty, the aggregated result otherwise
  ----
  function reduce = |this, init, func| -> match {
    when this: isPresent() then func: invoke(init, this: get())
    otherwise init
  }

  ----
  Remove one level of optional.

  This is actually equivalent to `:flatMap(identity)`
  (or `:flatMap(f)` is equivalent to `:map(f): flattened()`).

  For instance:

      Some(Some(42)): flattened() == Some(42)
      None(): flattened() == None()
      Some(None()): flattened() == None()
  ----
  function flattened = |this| -> this: flatMap(Function.identity())

  ----
  Convert this optional into a list

  - *return* a singleton list containing the value if present, otherwise an
    empty list
  ----
  function toList = |this| -> match {
    when this: isPresent() then singletonList(this: get())
    otherwise emptyList()
  }

  ----
  Same as `map` or `flatMap`, depending on the type returned by `f`.

  This is a generic version for `map` and `flatMap`: if `f` returns an
  `Optional`, it's equivalent to `flatMap`, otherwise, it's equivalent to `map`.

  This allows code such as:

      Some(21): andThen(|x| -> x + 1): andThen(|x| -> Some(2 * x)) == Some(42)
  ----
  function andThen = |this, f| {
    case {
      when this: isPresent() {
        let r = f: invoke(this: get())
        return match {
          when r oftype java.util.Optional.class then r
          otherwise java.util.Optional.ofNullable(r)
        }
      }
      otherwise {
        return this
      }
    }
  }

  ----
  Conjunctive chaining.

  This is equivalent to `:flatMap(|_| -> other)`,
  i.e. applicative or monadic sequence with discarding.
  (Can also be seen as `:map(const(^id)): apply(other)`)

  For instance:

      Some(1): `and(None()) == None()
      None(): `and(Some(1)) == None()
      Some(1): `and(Some("2")) == Some("2")

  Note that this method is eager. If you want laziness, use the `flatMap`
  equivalent.

  - *param* `other` the other optional
  - *return* `other` if this optional is present, otherwise `this`
  ----
  function `and = |this, other| -> match {
    when this: isPresent() then other
    otherwise this
  }

  ----
  Disjunctive chaining.

  For instance:

      Some(1): `or(Some(2)) == Some(1)
      None(): `or(Some(1)) == Some(1)
      Some(1): `or(None()) == Some(1)
      None(): `or(None()) == None()

  Note that this method is eager.

    Some(1): orElseGet(-> Some(2))
    None(): orElseGet(-> Some(2))

  - *param* `other` the other optional
  - *return* `other` if this optional is empty, otherwise `this`
  ----
  function `or = |this, other| -> match {
    when this: isPresent() then this
    otherwise other
  }

  function toResult = |this, param| -> match {
    when this: isSome() then
      gololang.error.Result.ok(this: get())
    when param oftype java.lang.Throwable.class then
      gololang.error.Result.error(param)
    otherwise
      gololang.error.Result.fail(param: toString())
  }

  function toResult = |this| ->
    this: toResult(NoSuchElementException("empty Optional"))

  ----
  Apply the function contained is this optional to the given optional. If the
  function has several parameters, an optional containing a partialized version
  is returned, that can be `apply`ed to subsequent optionals.
  This makes `Optional` an “applicative functor”.

  For instance:
      let f = Some(|x| -> x + 10)
      f: apply(Some(32)) # Some(42)
      f: apply(None())  # None()

      Some(|a, b| -> a + b): apply(Some(21)): apply(Some(21)) # Some(42)
  ----
  function apply = |this, arg| {
    case {
      when this: isPresent() {
        let f = this: get()
        require(isClosure(f), "The optional must contain a function to be applied")
        return match {
          when arg: isNone() then arg
          when f: arity() > 1 then Some(f: bindTo(arg: get()))
          otherwise Some(f: invoke(arg: get()))
        }
      }
      otherwise {
        return this
      }
    }
  }

  ----
  Case analysis for the option.

  If the result is not empty, apply the first function (similar to `flatMap`),
  otherwise invoke the second function (similar to `orElseGet`). For instance:

      Some(21): either(|x| -> x * 2, -> "plop") == 42
      None(): either(|x| -> x * 2, -> "plop") == "plop"

  This is indeed equivalent to

      opt: map(mapping): orElseGet(default)

  - *param* `mapping`: the function to apply to the contained value
  - *param* `default`: the function to invoke if the option is empty (takes no arguments)
  - *return* the result of applying the corresponding function
  ----
  function either = |this, mapping, default| -> match {
    when this: isPresent() then mapping(this: get())
    otherwise default()
  }
}

# ............................................................................ #
## Decorators

----
Transform a function raising an exception into a function returning a `Result`.
The resulting function returns `Result.ok(f(x))` if `f(x)` succeeds and
`Result.error(e)` if `f(x)` raises `e`.

Can be used as a decorator.
----
function result = |f| -> |args...| {
  try {
    return Result(f: invoke(args))
  } catch (e) {
    return Result(e)
  }
}

----
Transform a function raising an exception into a function returning a option.
The resulting function returns `Option(f(x))` if `f(x)` succeeds and `None` if
`f(x)` raises an exception.

Can be used as a decorator.

See also [`raising`](#raising_1)
----
function option = |f| -> |args...| {
  try {
    return Option(f: invoke(args))
  } catch (e) {
    return None()
  }
}

----
Execute the given block and return a `Result`.

This is similar to [`result`](#result_1), except that the block take no parameter and is
immediately executed. This is *not* a decorator, but rather a replacement for a
`try catch` block.

For instance:

    let res = trying({
      let f = foo()
      let x = bar()
      let z = f(x)
      return z + plop()
    })

If any of the operation in the block raises an exception, `res` will be a
`Result.error` containing the exception, otherwise it will be a `Result.ok`
containing the returned value.

See also [`result`](#result_1), [`catching`](#catching_1)
----
function trying = |f| {
  # TODO: later... make it a macro?
  try {
    return gololang.error.Result.ok(f())
  } catch (e) {
    return gololang.error.Result.error(e)
  }
}

----
Transforms a function returning a result or an option into a function returning
the associated value or raising an exception.

This is the inverse behavior of [`result`](#result_1) and [`option`](#option_1)

Can be used as a decorator.

See also [`result`](#result_1), [`option`](#option_1)
----
function raising = |f| -> |args...| -> f: invoke(args): get()

----
Transform a function into one that can return `null` if something went wrong
(i.e. None, Error or exception).

Can be used as a decorator.
----
function nullify = |f| -> |args...| {
  try {
    let r = f: invoke(args)
    return match {
      when r oftype java.util.Optional.class then r: get()
      when r oftype gololang.error.Result.class then r: get()
      otherwise r
    }
  } catch (e) {
    return null
  }
}

----
Allows an unary function to be called with an `Optional` or a `Result` value.
Similar to `flatMap`, but using `default` on the exception if the call failed.

For instance:

    let foo = |x| -> x: toUpperCase()

    let safeFoo = catching("plop")(foo)

    safeFoo(null)         # "plop"
    safeFoo(None())       # "plop"
    safeFoo(Error("err")) # "plop"
    safeFoo(Ok("a"))      # "A"
    safeFoo(Some("a"))    # "A"

    catching(^gololang.error.Result::error)(foo)(null) # Result.error(NullPointerException())

Can be used as a decorator.

- *param* `default`: the value to return if a exception occurred, or the function
  to apply to the exception.

See also [`trying`](#trying_1), [`result`](#result_1), [`option`](#option_1)
----
function catching = |default| -> |func| -> |val| {
  try {
    return func: invoke(val: get())
  } catch (e) {
    return match {
      when isClosure(default) then default: invoke(e)
      otherwise default
    }
  }
}

----
Create a catcher that execute the given block dealing with exceptions.

This is similar to [`trying`](#trying_1), but encapsulate the `catch` function
instead of returning a `Result`. It is also similar to [`catching`](#catching),
but the block is the block take no parameter and is immediately executed.

    let recover = catcher(|ex| -> match {
      when ex oftype IllegalArgumentException.class then "default"
      otherwise ""
    })

    let result = recover({
      let a = bar()
      let r = foo(42)
      return "foo: " + r
    )}

This function can be used as a decorator:

    @!catcher
    function recoverIAE = |ex| -> match {
      when ex oftype IllegalArgumentException.class then "default"
      otherwise ""
    }

    let result = recoverIAE({
      let a = bar()
      let r = foo(42)
      return "foo: " + r
    })

It is somewhat equivalent to a `trying` followed by `either` as in:

    let recover = |ex| -> match { ... }

    result = catcher(recover)({
      // some code that can fail
    })

    result = trying({
      // some code that can fail
    }): either(^gololang.Functions::id, recover)

- *param* `recover`: the value to return if a exception occurred, or the function
  to apply to the exception.

See also [`trying`](#trying_1), [`catching`](#catching)
----
function catcher = |recover| -> |block| {
  try {
    return block()
  } catch(e) {
    return match {
      when isClosure(recover) then recover(e)
      otherwise recover
    }
  }
}
