# ............................................................................................... #
#
# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
#
# ............................................................................................... #

----
This module defines utility functions and augmentations to ease the use of
the `gololang.LazyList` object.

A lazy list is an *immutable* list whose elements are evaluated only when needed,
as can be found in Haskell for example.

This is very useful when using higher order function such as `map`. Mapping
a long lazy list with a function and using only the 3 first elements will only
apply the function to these elements, as opposed to regular lists.

Lazy lists can also be used to create infinite lists, also known as generators
(or anamorphisms).

Lastly, they allow for elegant recursive implementations of several classical
algorithms.

For instance, one can create a infinite lazy list containing integers as:

    function count = |start| -> cons(start, -> count(start + 1_L))
    function count = -> count(0_L)

On the other hand, functions or methods like `equals`, `size` or `contains` are
not very efficients, since they must evaluate the whole list, and thus negate
the laziness. They are here for completeness and compatibility with the regular
lists interface, but you should avoid such methods.

Some functions in this module are recursive (re)implementation of standard list
HOF, such as `map` or `filter`. The recursive aspect should not be limiting since
the resulting list is lazy.
----
module gololang.LazyLists

import java.util

# ............................................................................................... #
# Utils, constructors and conversions

----
Returns the empty list.
----
function emptyList = -> gololang.LazyList.EMPTY()

----
Create a new lazy list from a head and tail values. Automatically wraps the
tail in a closure if its not already one.

For example:

    let myList = cons(1, cons(2, cons(3, emptyList())))

gives a lazy list equivalent to `list[1, 2, 3]`
----
function cons = |head, tail| -> match {
  when isClosure(tail) then gololang.LazyList.cons(head, tail)
  when tail is null then gololang.LazyList.cons(head, ^emptyList)
  otherwise gololang.LazyList.cons(head, -> tail)
}

----
Unary version of [`cons(head, tail)`](#cons_2).

Its parameter is assumed to be a tuple (or any object having a `get(idx)` method)
of the form `[head, tail]`.
----
function cons = |ht| -> cons(ht: get(0), ht: get(1))

----
Variadic function to create lazy lists from values.

    let myList = lazyList(1, 2, 3, 4)

is the equivalent to

    let myList = cons(1, cons(2, cons(3, cons(4, emptyList()))))
----
function lazyList = |values...| -> iteratorToLazyList(values: asList(): iterator())

----
Wraps any object implementing `Iterable` or `Iterator` in a lazy list.
The `next()` method of the underlying iterator is only called when the tail is
used.

NOTE:
If called with an `Iterator` instance, the iterator is shared, so navigating
through the list can have side effects if another object is using the same
iterator.
----
function fromIter = |it| -> match {
  when it oftype Iterable.class then
    iteratorToLazyList(it: iterator())
  when it oftype Iterator.class then
    iteratorToLazyList(it)
  otherwise raise("Invalid argument for fromIter")
}

augment java.lang.Iterable {
  ----
  Returns a lazy list from this `Iterable`. Can be used for instance to lazily
  map a list.
  ----
  function asLazyList = |this| -> iteratorToLazyList(this: iterator())
}

augment java.util.Iterator {
  ----
  Returns a lazy list view of this `Iterator`.
  ----
  function asLazyList = |this| -> iteratorToLazyList(this)
}

local function iteratorToLazyList = |iterator| {
  if not iterator: hasNext() {
    return gololang.LazyList.EMPTY()
  } else {
    let head = iterator: next()
    return gololang.LazyList.cons(head, -> iteratorToLazyList(iterator))
  }
}

# ............................................................................................... #

augment gololang.LazyList {

  ----
  Maps elements of a list using a function:

      lazyList(1, 2, 3):map(|x| -> 2 * x)

  `map` returns a new lazy list, i.e. `func` is applied only
  when necessary.

  This is a recursive implementation.
  ----
  function map = |this, func| -> match {
    when this: isEmpty() then gololang.LazyList.EMPTY()
    otherwise gololang.LazyList.cons(
      func(this: head()), -> this: tail(): map(func)
    )
  }

  ----
  Filters elements based on a predicate.

  Returns a new lazy list.
  ----
  function filter = |this, pred| -> match {
    when this: isEmpty() then gololang.LazyList.EMPTY()
    when pred(this: head()) then
      gololang.LazyList.cons(this: head(), -> this: tail(): filter(pred))
    otherwise this: tail(): filter(pred)
  }

  ----
  Finds the first element of a list matching a predicate:

      println(lazyList(1, 2, 3, 4): find(|n| -> n > 3))

  * `this`: a lazy list.
  * `pred`: a predicate function taking an element and returning a boolean.

  `find` returns `null` when no element satisfies `pred`.

  Note that in the worst case, all the list is search. Take care to **not use**
  this method on infinite list, since no check is made.
  ----
  function find = |this, pred| -> match {
    when this: isEmpty() then null
    when pred(this: head()) then this: head()
    otherwise this: tail(): find(pred)
  }

  ----
  Joins the elements into a string:

      println(list[1, 2, 3]: join(", "))

  * `this`: a list.
  * `separator`: the element separator string.

  The returned string is `""` when the list is empty.
  ----
  function join = |this, separator| {
    if this: isEmpty() {
      return ""
    }
    var it = this: iterator()
    var buffer = java.lang.StringBuilder(it: next(): toString())
    while it: hasNext() {
        buffer: append(separator)
        buffer: append(it: next())
    }
    return buffer: toString()
  }

  ----
  Folds left `this` using `func` with `zero` as initial value.
  This is a recursive implementation.

      lazyList(a, b, c): foldl(f, z) == f(f(f(z, a), b), c)

  Equivalent to `foldr` if `func` is commutative.
  ----
  function foldl = |this, func, zero| -> match {
    when this: isEmpty() then zero
    otherwise this: tail(): foldl(func, func(zero, this: head()))
  }

  ----
  Folds right `this` using `func` with `zero` as initial value.
  This is a recursive implementation.

      lazyList(a, b, c): foldr(f, z) == f(a, f(b, f(c, z)))

  Equivalent to `foldl` if `func` is commutative.
  ----
  function foldr = |this, func, zero| -> match {
    when this: isEmpty() then zero
    otherwise func(this: head(), this: tail(): foldr(func, zero))
  }


  ----
  Takes the `nb` first elements of the lazy list, as a lazy list.
  This is a wrapper, the underlying list is resolved on demand, such that
  everything remains lazy. `take` can thus be used on infinite lists.
  ----
  function take = |this, nb| -> match {
    when nb <= 0 or this: isEmpty() then emptyList()
    otherwise gololang.LazyList.cons(
      this: head(),
      -> this: tail(): take(nb - 1)
    )
  }

  ----
  Takes elements from the list as long as the given predicate is true.

  * `pred`: the predicate function used to end the list.
  ----
  function takeWhile = |this, pred| -> match {
    when this: isEmpty() or not pred(this: head()) then emptyList()
    otherwise gololang.LazyList.cons(this: head(), -> this: tail() :takeWhile(pred))
  }

  ----
  Remove `nb` elements from the list and return the rest as a lazy list.
  ----
  function drop = |this, nb| -> match {
    when nb <= 0 then this
    when this: isEmpty() then emptyList()
    otherwise this: tail(): drop(nb - 1)
  }

  ----
  Remove elements from the list as long as the given predicate is true.

  * `pred`: the predicate function used to end the list.
  ----
  function dropWhile = |this, pred| -> match {
    when this: isEmpty() then emptyList()
    when not pred(this: head()) then this
    otherwise this: tail(): dropWhile(pred)
  }

  ----
  Extract a lazy sublist.

  This is just a convenient method for `list: drop(from):take(to - from)`, so
  the list remains lazy.

  * `from`: low endpoint (inclusive) of the `subList`
  * `to`: high endpoint (exclusive) of the `subList`
  ----
  function subList = |this, from, to| -> this:drop(from):take(to - from)

}

----
Generator function on lazy lists (anamorphism).

This function generates a (possibly infinite) lazy list. Starting with the
`seed` value, if `finished(seed)` is `true`, the generation stops and an empty
list is returned. Otherwise, `unspool` is called on `seed`, and must generate
two values: the head of the list (current value) and the next seed that will be
used to generate the tail.

As an example, one can write a simple `range` function as:

    let range = |start, end| -> generator(
      |seed| -> [seed, seed + 1],
      |seed| -> seed >= end,
      start
    )

* `unspool`: the generative function
* `finished`: the condition function
* `seed`: the initial value
----
function generator = |unspool, finished, seed| {
  if finished(seed) {
    return gololang.LazyList.EMPTY()
  }
  let r = unspool(seed)
  return gololang.LazyList.cons(
    r:get(0),
    -> generator(unspool, finished, r:get(1))
  )
}

local function False = |args...| -> false

----
Produces a infinite list of values. If the argument is a closure, it must have
no parameters, and it's used to produce the values (called for each `tail`
access).

For instance, `repeat(5)` will return an infinite lazy list of `5`s, and
`repeat(^f)` will return a infinite lazy list of calls to `f`
([f(), f(), f(), ...])

* `value`: a value or a closure
----
function repeat = |value| -> match {
  when isClosure(value) then generator(|seed| -> [value(), null], ^False, null)
  otherwise generator(|seed| -> [value, null], ^False, null)
}

----
Returns an infinite lazy list produced by iterative application of a function
to an initial element.
`iterate(z, f)` thus yields `z, f(z), f(f(z)), ...`

For instance, one can create a infinite list of integers using:

    iterate(0, |x| -> x + 1)


* `zero`: the initial element of the list
* `func`: the function to apply
----
function iterate = |zero, func| -> generator(|seed| -> [seed, func(seed)], ^False, zero)

