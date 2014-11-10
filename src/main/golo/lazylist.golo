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

Some functions are recursive (re)implementation of standard list HOF, such as
`map` or `filter`. The recursive aspect should not be limiting since the
resulting list is lazy.
----
module gololang.lazylist

import java.util

# ............................................................................................... #
# Utils, constructors and conversions

local function head = |l| -> l:head()
local function tail = |l| -> l:tail()
local function isEmpty = |l| -> l:isEmpty()

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

----
Unary version of [`cons(head, tail)`](#cons_head_tail).

Its parameter is assumed to be a tuple (or any object having a `get(idx)` method)
of the form `[head, tail]`.
----
function cons = |ht| -> cons(ht:get(0), ht:get(1))

----
Variadic function to create lazy lists from values.

    let myList = lazyList(1, 2, 3, 4)

is the same as

    let myList = cons(1, cons(2, cons(3, cons(4, Empty()))))
----
function lazyList = |values...| -> iteratorToLazyList(values:asList():iterator())

----
Wraps any object implementing `Iterable` or `Iterator` in a lazy list.
The `next()` method of the underlying iterator is only called when the tail is
used.
----
function ittoLazyList = |it| -> match {
  when it oftype Iterable.class then
    iteratorToLazyList(it:iterator())
  when it oftype Iterator.class then
    iteratorToLazyList(it)
  otherwise raise("Invalid argument for ittoLazyList")
}

local function iteratorToLazyList = |iterator| {
  if not iterator:hasNext() {
    return gololang.LazyList.EMPTY()
  } else {
    let head = iterator:next()
    return gololang.LazyList(head, -> ittoLazyList(iterator))
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
    when any(lists:map(^isEmpty)) then Empty()
    otherwise gololang.LazyList(
      Tuple.fromArray(lists:map(^head):toArray()),
      -> zip(lists:map(^tail))
    )
  }
}


augment gololang.LazyList {
  ----
  Maps elements of a list using a function:

      lazyList(1, 2, 3):map(|x| -> 2 * x)

  `map` returns a new lazy list, i.e. `func` is applied only
  when necessary.

  This is just a convenient augmentation that delegates to
  [`gololang.lazylist.map`](#map_func_list)
  ----
  function map = |this, func| ->
    gololang.lazylist.map(func, this)
 
  ----
  Fold left on `this`.
  This is just a convenient augmentation that delegates to
  [`gololang.lazylist.foldl`](#foldl_func_zero_list)
  ----
  function foldl = |this, func, zero| ->
    gololang.lazylist.foldl(func, zero, this)

  ----
  Fold right on `this`.
  This is just a convenient augmentation that delegates to
  [`gololang.lazylist.foldr`](#foldr_func_zero_list)
  ----
  function foldr = |this, func, zero| ->
    gololang.lazylist.foldr(func, zero, this)

  ----
  Similar to
  [`reduce`](./StandardAugmentations#java.lang.Iterable.reduce_this_initialValue_func).

  This function only delegates to `foldl`, but is here to provide
  homogeneous interface with regular lists.
  ----
  function reduce = |this, initialValue, func| ->
    this:foldl(func, initialValue)

  #TODO: filter(this, func)
  #TODO: each(this, func)
  #TODO: find(this, pred)
  #TODO: join(this, separator)
}

#=== HOF ===
----
Converts `list` by applying `func` to each of its elements.
This is a recursive implementation.

    map(|x| -> 2 * x, list[1, 2, 3])

`list` can be any object having `head` and `tail` methods.
Returns a new lazy list, i.e. `func` is applied only when necessary.
----
function map = |func, list| -> match {
  when list:isEmpty() then Empty()
  otherwise gololang.LazyList(
    func(list:head()), -> map(func, list:tail())
  )
}

----
Folds left `list` using `func` with `zero` as initial value.
This is a recursive implementation.

    foldl(f, z, [a, b, c]) == f(f(f(z, a), b), c)

`list` can be any object having `head` and `tail` methods.
----
function foldl = |func, zero, list| -> match {
  when list:isEmpty() then zero
  otherwise foldl(func, func(zero, list:head()), list:tail())
}

----
Folds right `list` using `func` with `zero` as initial value.
This is a recursive implementation.

    foldr(f, z, [a, b, c]) == f(a, f(b, f(c, z)))

`list` can be any object having `head` and `tail` methods.
Equivalent to `foldl` if `func` is commutative.
----
function foldr = |func, zero, list| -> match {
  when list:isEmpty() then zero
  otherwise func(list:head(), foldr(func, zero, list:tail()))
}

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

function filter = |pred, llist| -> match {
  when llist:isEmpty() then Empty()
  when pred(llist:head()) then
    gololang.LazyList(llist:head(), -> filter(pred, llist:tail()))
  otherwise filter(pred, llist:tail())
}

function take = |nb, list| -> match {
  when nb == 0 then Empty()
  when list:isEmpty() then Empty()
  otherwise gololang.LazyList(
    list:head(),
    -> take(nb - 1, list:tail())
  )
}

#TODO: takeWhile
#TODO: drop
#TODO: dropWhile
#TODO: count
#TODO: enumerate
#TODO: ...
