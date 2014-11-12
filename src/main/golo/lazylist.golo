# ............................................................................................... #
#
# Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ............................................................................................... #

----
This module defines utility functions and augmentations to ease the use of
the `gololang.LazyList` object.

A lazy list is a *immutable* list that is evaluated only when needed, as can be
found in Haskell for example.

This is very useful when using higher order function such as `map`. Mapping
long a lazy list with a function and using only the 3 first elements will only
apply the function to these elements, as oposed to regular lists.

Lazy lists can also be used to create infinite lists or generators.

Lastly, they allow for elegant recursive implementations of several classical
algorithms.

On the other hand, functions or methods like `equals`, `size` or `contains` are
not very efficients, since they must evaluate the whole list, and thus negate
the lazyness. They are here for completeness and compatibility with the regular
lists interface, but you should avoid such methods.

Some functions in this module are recursive (re)implementation of standard list
HOF, such as `map` or `filter`. The recursive aspect should not be limiting since
the resulting list is lazy.
----
module gololang.lazylist

import java.util

# ............................................................................................... #
# Utils, constructors and conversions

local function head = |l| -> l: head()
local function tail = |l| -> l: tail()
local function isEmpty = |l| -> l: isEmpty()

----
Returns the empty list.
----
function Empty = -> gololang.LazyList.EMPTY()

----
Create a new lazy list from a head and tail values. Automatically wraps the
tail is a closure if its not already one.

For example:

    let myList = cons(1, cons(2, cons(3, Empty())))

gives a lazy list equivalent to `list[1, 2, 3]`

----
function cons = |head, tail| -> match {
  when isClosure(tail) then gololang.LazyList(head, tail)
  otherwise gololang.LazyList(head, -> tail)
}

#----
#Unary version of [`cons(head, tail)`](#cons_head_tail).
#
#Its parameter is assumed to be a tuple (or any object having a `get(idx)` method)
#of the form `[head, tail]`.
#----
#function cons = |ht| -> cons(ht: get(0), ht: get(1))

----
Variadic function to create lazy lists from values.

    let myList = lazyList(1, 2, 3, 4)

is the same as

    let myList = cons(1, cons(2, cons(3, cons(4, Empty()))))
----
function lazyList = |values...| -> iteratorToLazyList(values: asList(): iterator())

----
Wraps any object implementing `Iterable` or `Iterator` in a lazy list.
The `next()` method of the underlying iterator is only called when the tail is
used.
----
function fromIter = |it| -> match {
  when it oftype Iterable.class then
    iteratorToLazyList(it: iterator())
  when it oftype Iterator.class then
    iteratorToLazyList(it)
  otherwise raise("Invalid argument for fromIter")
}

local function iteratorToLazyList = |iterator| {
  if not iterator: hasNext() {
    return gololang.LazyList.EMPTY()
  } else {
    let head = iterator: next()
    return gololang.LazyList(head, -> iteratorToLazyList(iterator))
  }
}

# ............................................................................................... #

local function any = |it| {
  foreach elt in it {
    if elt {
      return true
    }
  }
  return false
}


function zip = |lists| {
  return match {
    when any(lists: map(^isEmpty)) then Empty()
    otherwise gololang.LazyList(
      Tuple.fromArray(lists: map(^head): toArray()),
      -> zip(lists: map(^tail))
    )
  }
}


augment gololang.LazyList {

  ----
  Maps elements of a list using a function:

      lazyList(1, 2, 3):map(|x| -> 2 * x)

  `map` returns a new lazy list, i.e. `func` is applied only
  when necessary.

  This is a recursive implementation.
  ----
  function map = |this, func| -> match {
    when this: isEmpty() then Empty()
    otherwise gololang.LazyList(
      func(this: head()), -> this: tail(): map(func)
    )
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
  ----
  function zip = |this, others...| ->
    gololang.lazylist.zip(java.util.ArrayList(others: asList()): prepend(this))

  ----
  Takes the `nb` first elements of the lazy list, as a lazy list.
  This is a wrapper, the underlying list is resolved on demand, such that
  everything remains lazy. `take` can thus be used on infinite lists.
  ----
  function take = |this, nb| -> match {
    when nb == 0 or this: isEmpty() then Empty()
    otherwise gololang.LazyList(
      this: head(),
      -> this: tail(): take(nb - 1)
    )
  }

  ----
  ----
  function takeWhile = |this, pred| -> match {
    when this: isEmpty() or not pred(this: head()) then Empty()
    otherwise gololang.LazyList(this: head(), -> this: tail() :takeWhile(pred))
  }
  
  ----
  ----
  function drop = |this, nb| -> match {
    when nb == 0 then this
    when this: isEmpty() then Empty()
    otherwise this: tail(): drop(nb - 1)
  }

  ----
  ----
  function dropWhile = |this, pred| -> match {
    when this: isEmpty() then Empty()
    when not pred(this: head()) then this
    otherwise this: tail(): dropWhile(pred)
  }
  ----
  Filters elements based on a predicate.

  Returns a new lazy list.
  ----
  function filter = |this, pred| -> match {
    when this: isEmpty() then Empty()
    when pred(this: head()) then
      gololang.LazyList(this: head(), -> this: tail(): filter(pred))
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
  Join the elements into a string:

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
  ----
  function enumerate = |this| -> this: zip(count())
}

#=== HOF ===


function generator = |unspool, finished, x| {
  if finished(x) {
    return Empty()
  }
  let r = unspool(x)
  return gololang.LazyList(
    r:get(1),
    -> generator(unspool, finished, r:get(1))
  )
}

function count = |start| -> 
  gololang.LazyList(start, -> gololang.lazylist.count(start + 1))

function count = -> gololang.lazylist.count(0)

