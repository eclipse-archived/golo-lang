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
This module contains several functions and higher order functions or
decorators, mostly useful with HOF like `map`, `reduce` or in generators.

Indeed, using HOF often require some classical functions like constants one,
identity, or classical operators.

Most of the times, these functions needs to be unary ones, i.e.
have only one parameter. This module contains, among others, functions helping in the arity
conversion of such functions:

* [`curry`](#curry_1) that makes a function automatically partializable,
* [`uncurry`](#uncurry_1) that is the reverse of `curry`,
* [`unary`](#unary_1) and [`spreader`](#spreader_1) that convert a polyadic function into a unary
  function taking an array or a tuple of values instead of several
  values,
* [`varargs`](#varargs_1) which is the reverse of `unary`

Most of the binary operator functions defined here are curried. To ease
partialization, the non commutative operators have a reversed version, with
arguments swapped.
For example, using the reverse division operator [`rdiv`](#rdiv_2) with
[`map`](StandardAugmentations.html#java.util.List.map_2)

    [2, 4, 8]: map(rdiv(2)) == [1, 2, 4]

The identity function [`id`](#id_1), the constant function
[`const`](#const_1), together with composition (`FunctionReference::andThen`,
[`pipe`](#pipe_1), [`compose`](#compose_1)) and [`io`](#io_1) can be
used to create monad-like processing chains.
----
module gololang.Functions

import java.lang.invoke
import java.util

### Operators ##########################################################

#== Arithmetic =========================================================
----
Polymorphic addition and concatenation.

Adds `value` to `element`.
The behavior depends on the type of `element`:

- if a number or a string, equivalent to using `+`;
- if a collection, equivalent to invoking `add` and returning the collection;
- if an `Appendable` or a `StringBuilder`, equivalent to invoking `append` and
  returning the collection.

This function is *swapped* and curried. For instance:

    add("b", "a") == "ab"

    list[1, 2, 3]: map(add(3)) == list[4, 5, 6]

    add("a", StringBuilder()): toString() == "a"

    let f = add(42): andThen(add(1337)): andThen(add(69))
    f(list[]) == list[42, 1337, 69]
    f(0) == 1448
    f(StringBuilder()): toString() == "42133769"


- *param* `value`: the value to add
- *param* `element`: the element to add the value to

See also [`addTo`](#addTo_2)
----
@!curry
function add = |value, element| {
  case {
    when _addWithPlus(element) {
      return element + value
    }
    when _addWithAdd(element)  {
      element: add(value)
      return element
    }
    when _addWithAppend(element) {
      element: append(value)
      return element
    }
    otherwise {
      throw IllegalArgumentException("Don't know how to add to a %s"
            : format(element: getClass(): getName()))
    }
  }
}

local function _addWithPlus = |elt| -> elt oftype java.lang.Number.class
                                    or elt oftype java.lang.String.class

local function _addWithAdd = |elt| -> elt oftype java.util.Collection.class
                                   or elt oftype java.util.stream.Stream$Builder.class
                                   or elt oftype java.util.StringJoiner.class

local function _addWithAppend = |elt| -> elt oftype java.lang.Appendable.class
                                      or elt oftype java.lang.StringBuffer.class
                                      or elt oftype java.lang.StringBuilder.class

----
Not swapped version of [`add`](#add_2)
----
@!curry
function addTo = |element, value| -> add(value, element)

----
Number successor (side-effect free increment).

    succ(x) == x + 1

Equivalent to `add(1)`

See also [`pred`](#pred_1)
----
function succ = |x| -> x + 1

----
Number predecessor (side-effect free decrement).

    pred(x) == x - 1

See also [`succ`](#succ_1)
----
function pred = |x| -> x - 1

----
Curried multiplication.

    mul(x, y) == x * y

See also [`div`](#div_2)
----
@!curry
function mul = |x, y| -> x * y


----
Opposite (negative value).

    neg(x) == -x

Same as `mul(-1)`
----
function neg = |x| -> -1 * x

----
Curried subtract.

    sub(x, y) == x - y

See also [`add`](#add_2), [`rsub`](#rsub_2)
----
@!curry
function sub = |x, y| -> x - y

----
Reversed curried subtract.

    rsub(x, y) == y - x

See also [`sub`](#sub_2)
----
@!curry
function rsub = |x, y| -> y - x

----
Curried division.

    div(x, y) == x / y

See also [`mul`](#mul_2), [`rdiv`](#rdiv_2)
----
@!curry
function div = |x, y| -> x / y

----
Reverse curried division.

    rdiv(y, x) == x / y

See also  [`mul`](#mul_2),  [`div`](#div_2)
----
@!curry
function rdiv = |x, y| -> y / x

----
Curried modulo.

    mod(a, b) == a % b

See also [`rmod`](#rmod_2)
----
@!curry
function mod = |a, b| -> (a % b)

----
Reverse curried modulo.

    rmod(a, b) == b % a

See also [`mod`](#mod_2)
----
@!curry
function rmod = |a, b| -> (b % a)

----
Curried power.

    pow(a, 2) == a*a

Does an implicit conversion to double.

See also [`rpow`](#rpow_2)
----
@!curry
function pow = |a, b| -> Math.pow(doubleValue(a), doubleValue(b))

----
Reversed curried power.

    rpow(2, a) == a*a

Does an implicit conversion to double.

See also [`pow`](#pow_2)
----
@!curry
function rpow = |a, b| -> Math.pow(doubleValue(b), doubleValue(a))


----
Converts its argument to a `String`.
----
function str = |o| -> o: toString()


#== Boolean operators ==================================================

----
Curried swapped *less than*.

    lt(a, b) == b < a

    lt(42) == |x| -> x < 42

Note: this function is swapped to make curried version more readable: `lt(42)`
is a predicate testing if its argument is "less than 42".

See also [`le`](#le_2), [`gt`](#gt_2), [`ge`](#ge_2)
----
@!curry
function lt = |a, b| -> b < a

----
Curried swapped *greater than*.

    gt(a, b) == b > a

Note: this function is swapped to make curried version more readable.

See also [`lt`](#lt_2), [`le`](#le_2), [`ge`](#ge_2)
----
@!curry
function gt = |a, b| -> b > a

----
Curried *equal*.

    eq(a, b) == (a == b)

See also [`ne`](#ne_2)
----
@!curry
function eq = |a, b| -> a == b

----
Curried *not equal*.

    ne(a, b) == (a != b)

See also [`eq`](#eq_2)
----
@!curry
function ne = |a, b| -> a != b

----
Curried swapped *greater than or equal*.

    ge(a, b) == (b >= a)

Note: this function is swapped to make curried version more readable.

See also [`lt`](#lt_2), [`le`](#le_2), [`gt`](#gt_2)
----
@!curry
function ge = |a, b| -> b >= a

----
Curried swapped *less than or equal*.

    le(a, b) == (a <= b)

Note: this function is swapped to make curried version more readable.

See also [`lt`](#lt_2), [`ge`](#ge_2), [`gt`](#gt_2)
----
@!curry
function le = |a, b| -> b <= a

----
Curried boolean *and*.

This implementation is lazy.

The arguments can be boolean expressions or predicate functions. If called with
predicate functions, returns a new predicate function.

    `and(a, b) == (a and b)
    `and(gt(10), ^even) == |x| -> a > 10 and x % 2 == 0

See also [`or`](#or_1), [`not`](#not_1), [`xor`](#xor_2)
----
@!uncurry
function `and = |a| -> match {
  when isClosure(a) then curry(|b, x| -> a(x) and b(x))
  when a then ^id
  otherwise ^False
}

----
Curried boolean *or*.

    `or(a, b) == (a or b)

This implementation is lazy.

The arguments can be boolean expressions or predicate functions. If called with
predicate functions, returns a new predicate function.

See also [`and`](#and_1), [`not`](#not_1), [`xor`](#xor_2)
----
@!uncurry
function `or = |a| -> match {
  when isClosure(a) then curry(|b, x| -> a(x) or b(x))
  when a then ^True
  otherwise ^id
}

----
Polymorphic negation function.

* when given a boolean-like value, returns its negation.
* when given a predicate function, returns a new function returning the
  negated result.

E.g.

    list[true, false, false]: map(^not) == list[false, true, true]

    let even = |a| -> (a % 2) == 0
    let odd = `not(even)

See also [`and`](#and_1), [`or`](#or_1)
----
function `not = |a| -> match {
  when isClosure(a) then a: andThen(|v| -> not v)
  otherwise not a
}

----
Curried boolean *xor*.

    xor(a, b) == (a or b) and not (a and b)

The arguments can be boolean expressions or predicate functions. If called with
predicate functions, returns a new predicate function.

See also [`and`](#and_1), [`or`](#or_1)
----
@!curry
function xor = |a, b| -> match {
  when isClosure(a) then curry(|b, x| -> _xor(a(x), b(x)))
  otherwise _xor(a, b)
}

local function _xor = |a, b| -> (a or b) and not (a and b)

----
Checks if the value is even.

See also [`odd`](#odd_1)
----
function even = |a| -> (a % 2) == 0

----
Checks if the value is odd.

See also [`even`](#even_1)
----
function odd = |a| -> (a % 2) == 1

----
Swapped curried containment test.

Checks if `collection` contains `value`.
The collection object can be anything with a `contains` method.

For instance:

    let predicate = contains(42)
    if (predicate(collection)) {
      ...
    }

- *param* `value`: the value to test for containment
- *param* `collection`: any object having a `contains` method
----
@!curry
function contains = |value, collection| -> collection:contains(value)

----
Emptiness test.

Checks if the collection-like argument is empty.
----
function isEmpty = |o| -> o: isEmpty()

----
Curried identity comparison

    `is(ref, obj) == obj is ref

Parameters are swapped to ease partialization, as in:

    lst: filter(is(ref))
----
@!curry
function `is = |ref, obj| -> obj is ref

----
Curried difference comparison

    `isnt(ref, obj) = obj isnt ref

Like `is`, this is more useful partialized. For instance:

    lst: filter(`isnt(null))
----
@!curry
function `isnt = |ref, obj| -> obj isnt ref

----
Curried type checking

    `oftype(type, obj) = obj oftype type

Partialized version can be used as a predicate, for instance in `filter`:

    alist: filter(`oftype(String.class))

----
@!curry
function `oftype = |type, object| -> object oftype type

----
Null value substitution

    `orIfNull(0, 3) == 3
    `orIfNull(0, null) == 0

This function can be useful, for example, to replace `null` values in a list with
a map:

    list[1, 2, null, 3]:map(`orIfNull(0)) == list[1, 2, 0, 3]
----
@!curry
function `orIfNull = |fallback, value| -> value orIfNull fallback


#=== Destructuring =====================================================

----
First element of a list, tuple, vector, array.

    fst([a, b]) == a

- *param* `t`: any object with a `get` method and at least one element.
- *throws* an `IndexOutOfBoundsException` if the object is empty.
----
function fst = |t| -> t: get(0)

----
Second element of a list, tuple, vector, array.

    snd([a, b]) == b

- *param* `t`: any object with a `get` method and at least two elements.
- *throws* an `IndexOutOfBoundsException` if the object has less than 2 elements.
----
function snd = |t| -> t: get(1)

----
Curried indexing.

    getitem(obj, i) == obj: get(i)

Throws an `IndexOutOfBoundsException` (for a collection-like object) or returns
`null` (for a map-like object) if the index is not present.

- *param* `obj`: any object having a `get` method
- *param* `i`: the index to get

See also [`setitem`](#setitem_3), [`getter`](#getter_1)
----
@!curry
function getitem = |obj, i| -> obj: get(i)

----
Curried indexed assignment

    setitem(obj, i, v) == obj: set(i, v)

- *param* `obj`: any object having a `set` method
- *param* `i`: the index to set
- *param* `v`: the value to set

See also [`getitem`](#getitem_2), [`getter`](#getter_1)
----
@!curry
function setitem = |obj, i, v| {
  obj: set(i, v)
  return obj
}

----
Indexer factory (curried indexing).

    let third = getter(2)
    third([1, 2, 3, 4]) -> 3

See also [`getitem`](#getitem_2)
----
function getter = |i| -> |col| -> col: get(i)


####################################################################
----
The identity function.

Returns it's argument unchanged. This can be useful with
*higher order functions* (*I* combinator).
----
function id = |x| -> x

----
The constant function.

Returns a new variadic functions that always returns `val`, ignoring its
arguments. For example:

    let theAnswerTo = const(42)

    theAnswerTo() == 42
    theAnswerTo(1, 2, 3) == 42
    theAnswerTo("Life, the Universe and Everything") == 42

One interesting use case is to write "pipe" like composition, with the argument
on the left:

    const(42): andThen(f): andThen(g): andThen(h)() == h(g(f(42)))

This is also the *K* combinator.
----
function const = |val| -> |_...| -> val

----
`false` constant function.
----
function False = |_...| -> false

----
`true` constant function.
----
function True = |_...| -> true

----
`null` constant function.
----
function Null = |_...| -> null


#== Changing a function arity =============================================
----
Function to curryfy a function, i.e. transforms a polyadic function into a
sequence of variadic functions. This allows for automatic partial application.

This is equivalent to applying `invokeOrBind` on each call.

E.g.

    function f = |a, b, c| -> a + b + c

    let g = curry(^f)

    g(29)(10)(3) == f(29, 10, 3)
    g(29, 10)(3) == 42
    g(29)(10, 3) == 42

    let g1 = g(29)
    let g2 = g1(10)
    let g3 = g(29, 10)

    g(29, 10, 3) == 42
    g1(10, 3) == 42
    g1(10)(3) == 42
    g2(3) == 42
    g3(3) == 42

This is particularly useful to allow composing polyadic functions. For
instance, with the previous `g`:

    let h = g(2, 20): andThen(g("The answer", " is "))
    h(20) == "The answer is 42"

If the function is variadic, only the fixed parameters can be partialized.
For instance, given:

    @!curry
    function f = |a, b, c...| -> ...
    ...
    let g = f(1, 2)

then `g` is a variadic function that can't be called partially. All the
following calls are valids:

    g()
    g(1)
    g(1, 2, 3)

but

    g(1)(2)

is not.

This function can also be used as a decorator.

- *param* `f` the function to curry
- *return* a variadic function dispatching on `f` that can be partialized

See also [`uncurry`](#uncurry_1)
----
function curry = |f| -> match {
  when f: arity() < 2 then f
  otherwise |args...| {
    let result = f: invokeOrBind(args)
    return match {
      when isClosure(result) then curry(result)
      otherwise result
    }
  }
}

----
Reverse of [`curry`](#curry_1).

Take a curried function (e.g. `|a| -> |b| -> a + b`) and return a polyadic
function (e.g. `|a, b| -> a + b`).
It is actually a variadic function calling the curried one in sequence.

The two functions defined by

    let f = curry(|a, b, c| -> a + b + c)
    let g = uncurry(|a| -> |b| -> |c| -> a + b + c)

have the same behavior.

This function can also be used as a decorator.

- *param* `f` the function to uncurry
- *return* a variadic function dispatching on `f`

See also [`curry`](#curry_1)
----
function uncurry = |f| -> match {
  when f: isVarargsCollector() then f
  otherwise |args...| {
    var uc = f
    var i = 0
    var argslength = args: length()
    while i < argslength and not uc: isVarargsCollector() {
      uc = uc(args: get(i))
      i = i + 1
    }
    return match {
      when i < argslength then uc: invoke(Arrays.copyOfRange(args, i, argslength))
      when isClosure(uc) then uncurry(uc)
      otherwise uc
    }
  }
}

----
Convers a polyadic function into an unary function.
E.g.

    let f = |a, b, c| -> a + b + c
    let g = unary(f)

    g([1, 2, 3]) == 6

If the function already takes zero or one argument, it is returned
unchanged.

This can be useful in HOF, for example to `map` a binary function on a list of
couple:

    let f = |a, b| -> ...
    list[["a", 1], ["b", 2], ["c", 3]]: map(unary(f))

results in `list[f("a", 1), f("b", 2), f("c", 3)]`

The resulting unary function will accept any object having a `toArray` method
(array, tuple, collection, map entry, struct...)

Thus, `unary(func)(arg)` is equivalent to `func: invoke(arg: toArray())`

This function can also be used as a decorator.

See also [`spreader`](#spreader_1) and [`varargs`](#varargs_1)
----
function unary = |f| -> |args| -> f: invoke(args: toArray())

----
Convers a polyadic function into an unary function.

Similar to [`unary`](#unary_1) but using `spread` instead of `invoke`.

This function can also be used as a decorator.

See also [`varargs`](#varargs_1)
----
function spreader = |f| -> |args| -> f: spread(args: toArray())

----
Convert an unary function taking an array into a variadic function.

This is the contrary of [`unary`](#unary_1).
E.g.

    let f = |t| -> t:get(0) + t:get(1) + t:get(2)
    let g = varargs(f)

    f([1, 2, 3]) == g(1, 2, 3)

This function can also be used as a decorator.

See also [`spreader`](#spreader_1) and [`unary`](#unary_1)
----
function varargs = |f| -> f: asVarargsCollector(objectArrayType())

#=== Other signature changing HOF =============================================

----
Change the signature of the given binary function by swapping its arguments,
such that:

    swapArgs(f)(b, a) == f(a, b)

*Warning*: when using this function with one previously wrapped in
[`curry`](#curry_1) or [`uncurry`](#uncurry_1), the resulting function can't be
automatically partialized any more.
----
function swapArgs = |func| {
  return match {
    when func: arity() == 2 and func: isVarargsCollector() then
      |a, b| -> func: asFixedArity()(b, a)
    when func: arity() == 1 and func: isVarargsCollector() then
      |args...| -> func: invoke(swapCouple(args))
    when func: arity() == 2 then |a, b| -> func: invoke(b, a)
    otherwise raise("Can only swap binary functions")
  }
}

----
Change the signature of the given curried binary function by swapping its
arguments, such that:

    swapCurry(f)(a)(b) = f(b)(a)
----
function swapCurry = |func| -> |a| -> |b| -> func(b)(a)


local function isCollection = |v| -> (
  v oftype java.util.List.class
  or v oftype gololang.Tuple.class
  or v oftype java.util.RandomAccess.class
  or isArray(v)
)

----
Swap the values of a couple.

The couple can be a tuple, a vector, a list or an array as long as its size is 2.
The return value as the same type.

For example:

    swap([a, b]) == [b, a]
----
function swapCouple = |couple| {
  require(couple: size() == 2, "Can only swap a couple")
  let a = couple: get(1)
  let b = couple: get(0)
  return match {
    when couple oftype java.util.List.class then list[a, b]
    when couple oftype gololang.Tuple.class then tuple[a, b]
    when couple oftype java.util.RandomAccess.class then vector[a, b]
    when isArray(couple) then array[a, b]
    otherwise
      raise("Can't swap a " + couple:getClass():getName())
  }
}

----
Polymorphic swapping.

This function dispatches on [`swapArgs`](#swapArgs_1),
[`swapCurry`](#swapCurry_1) or [`swapCouple`](#swapCouple_1) according
to its parameter.

Note that we have the property that given a binary function `f` and a
couple `c`

    unary(f)(swap(c)) == unary(swap(f))(c)

When applied to functions, equivalent to the *C* combinator.
----
function swap = |v| -> match {
  when isClosure(v) and v:arity() == 1 and not v: isVarargsCollector() then swapCurry(v)
  when isClosure(v) then swapArgs(v)
  when isCollection(v) then swapCouple(v)
  otherwise
    raise("Can't swap a " + v:getClass():getName())
}


#==== Composition and application =============================================

----
Reversed curried function application.

Apply the given function to the given value.

    invokeWith("a", "b", "c")(f) == f("a", "b", "c")

For instance, given a list of functions:

    list[f, g, h]: map(invokeWith(42)) == list[f(42), g(42), h(42)]

It can also be used to promote a value in continuation passing style.

- *param* `args` the values on which apply the function
----
function invokeWith = |args...| -> |f| -> f: invoke(args)

----
Function chaining.

This is similar to Unix pipe:

    pipe(f1, f2, f3)(x) == f3(f2(f1(x))))

i.e. apply `f1` to `x`, then pass it to `f2`, and then `f3`.
This is the same as `f1: andThen(f2): andThen(f3)(x)`

You can insert a side-effect only function in the chain using [`io`](#io_1):

    pipe(f1, io({println("hello")}), f2)(42)

is equivalent to:

    let tmp = f1(x)
    println("hello")
    f2(tmp)

This is similar to [`compose`](#compose_1), but with functions order
reversed.

Example:

    let cmd = pipe(mul(2), add(4), ^toString, addTo("val: "))
    cmd(19) == "val: 42"
    let other = pipe(^intValue, add(4), mul(2))
    other("17") == 42

If no function is given, returns [`id`](#id_1)

For a similar construct that deal with errors, see for instance the
[`gololang.Errors`](Errors.html#java.util.Optional.andThen_this_f) module
----
function pipe = |funcs...| {
  var r = ^id
  foreach f in funcs {
    r = r: andThen(f)
  }
  return r
}

----
Function composition.

    compose(f1, f2, f3)(x) == f1(f2(f3(x)))

This is similar to [`pipe`](#pipe_1), but with functions order
reversed (*B* combinator).
----
function compose = |funcs...| {
  var r = ^id
  for (var i = funcs: length() - 1, i >= 0, i = i - 1) {
    r = r: andThen(funcs: get(i))
  }
  return r
}

----
Transform a function with side effects (generally IO operations, thus the name)
into a function applying the side effect. If the wrapped function returns
`null`, its argument is return instead.

The given function can have no parameters, or accept an argument.

This can be used to insert such a function into a composition chain:

    f1: andThen(io({println("hello")})): andThen(f2)

is an anonymous function equivalent to:

    |x| {
      var result = f1(x)
      println("hello")
      return f2(result)
    }

and

    f1: andThen(io(|x|{println("got " + x)})): andThen(f2)

is an anonymous function equivalent to:

    |x| {
      var result = f1(x)
      println("got " + result)
      return f2(result)
    }

One last example with reading IO effect:

    f1: andThen(io(|x| {
      println("Got " + x)
      return x + intValue(readln("Value to add? "))})
    : andThen(f2)

is an anonymous function equivalent to:

    |x| {
      var result = f1(x)
      println("Got " + x)
      result = result + intValue(readln("Value to add? "))
      return f2(result)
    }

- *param* `block` a function with side effects

See also [`pipe`](#pipe_1) and [`compose`](#compose_1)
----
function io = |block| -> |x| -> match {
    when block: arity() == 1 then block(x)
    otherwise block()
  } orIfNull x

----
Apply the given function until the predicate holds.

For instance:

    let f = until(gt(10), mul(2))
    f(15) == 15
    f(2) == 16
    f(9) == 18
----
function until = |predicate, fun| -> |x| {
  var r = x
  while not predicate: invoke(r) {
    r = fun: invoke(r)
  }
  return r
}

----
Fixed-point combinator.

This is an abstraction of recursion.

- *param* `f` the function that will recur
- *retun* a function that makes recursive call to `f`
----
function recur = |f| -> (|x| -> f(|y| -> x(x)(y)))((|x| -> f(|y| -> x(x)(y))))

----
Conditional application combinator.

Equivalent to using the `match` construct:

    let r = cond(predicate, ifTrue, ifFalse, x)

    # equivalent to
    let r = match {
      when predicate(x) then ifTrue(x)
      otherwise ifFalse(x)
    }

Since this function is curried, it can be used as:

    let f = cond(predicate, ifTrue, ifFalse)

    # equivalent to
    let f = |x| -> match {
      when predicate(x) then ifTrue(x)
      otherwise ifFalse(x)
    }

- *param* `predicate` a boolean function
- *param* `ifTrue` the unary function to apply if `predicate` holds
- *param* `ifFalse` the unary function to apply otherwise
----
@curry
function cond = |predicate, ifTrue, ifFalse, x| -> match {
  when predicate(x) then ifTrue(x)
  otherwise ifFalse(x)
}
