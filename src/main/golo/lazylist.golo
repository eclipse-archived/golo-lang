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

----
Converts a lazy list into a list.
----
function lltoList = |lazylist| {
  let lst = list[]
  var ll = lazylist
  while not ll:isEmpty() {
    lst: add(ll: head())
    ll = ll: tail()
  }
  return lst
}

augment gololang.LazyList {
  ----
  Converts a lazy list into a list.
  ----
  function asList = |this| -> lltoList(this)
}


# ............................................................................................... #
local function ltoTuple = |v| -> Tuple.fromArray(v:toArray())

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
      ltoTuple(lists:map(^head)),
      -> zip(lists:map(^tail))
    )
  }
}

#=== HOF ===
function lmap = |func, llist| -> match {
  when llist:isEmpty() then Empty()
  otherwise gololang.LazyList(
    func(llist:head()),
    -> lmap(func, llist:tail())
  )
}

function foldl = |func, zero, llist| -> match {
  when llist:isEmpty() then zero
  otherwise foldl(func, func(zero, llist:head()), llist:tail())
}

function foldr = |func, zero, llist| -> match {
  when llist:isEmpty() then zero
  otherwise func(llist:head(), foldr(func, zero, llist:tail()))
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

#TODO: take
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
