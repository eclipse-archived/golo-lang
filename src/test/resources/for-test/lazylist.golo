# ............................................................................................... #
#
# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
# ............................................................................................... #

module golo.test.LazyList

import gololang.LazyLists
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

import org.eclipse.golo.runtime


local function longLL = ->
      LazyList.cons(1, -> LazyList.cons(2, -> LazyList.cons(3, -> LazyList.cons(4, -> LazyList.cons(5, -> emptyList())))))

local function longL = -> list[1, 2, 3, 4, 5]

#========================================================
function test_empty = -> (
  emptyList():isEmpty()
  and (emptyList() is emptyList())
  and lazyList() is emptyList()
)

function test_head = {
  let ll = LazyList.cons(1, -> emptyList())
  return [ll: head(), 1]
}

function test_tail = {
  let ll = LazyList.cons(1, -> LazyList.cons(2, -> emptyList()))
  return [ll:tail():head(), 2]
}

function test_tail_empty = {
  let ll = LazyList.cons(1, -> emptyList())
  return [ll:tail():isEmpty(), true]
}

function test_head_tails = {
  let ll = longLL()
  return [
    [ll:head(), ll:tail():head(), ll:tail():tail():head(),
     ll:tail():tail():tail():head(),
     ll:tail():tail():tail():tail():head(),
     ll:tail():tail():tail():tail():tail()],
    [1, 2, 3, 4, 5, emptyList()]
  ]
}

function test_size = -> [longLL(): size(), 5]

function test_equals = -> [
  [longLL(), longLL()],
  [longLL(), lazyList(1, 2, 3, 4, 5)]
]

function test_from_iter = -> [fromIter(longL()), longLL()]

function test_to_list = -> [
  [longLL(): asList(), emptyList(): asList()],
  [longL(), list[]]
]


function test_get = -> [longLL():get(3), 4]

function test_indexOf = -> [
  [longLL():indexOf(4), longLL():indexOf(42)],
  [3, -1]
]

function test_contains = -> [
  [longLL():contains(4), longLL():contains(42)],
  [true, false]
]

function test_containsAll = -> [
  [longLL():containsAll(list[2, 4]),
  longLL():containsAll(list[2, 4, 8])],
  [true, false]
]

function test_iterable = {
  let res = list[]
  foreach elt in longLL() {
    res: add(elt)
  }
  return [res, longL()]
}

function test_constVar = -> [lazyList(1, 2, 3, 4, 5), longLL()]
function test_cons = -> [
  cons(1, cons(2, cons(3, cons(4, cons(5, emptyList()))))),
  longLL()
]

function test_map = {
  let res = LazyList.cons(2, -> LazyList.cons(4, -> LazyList.cons(6, ->
  LazyList.cons(8, -> LazyList.cons(10, -> emptyList())))))
  return [longLL():map(|a| -> 2* a), res]
}

function test_mapEmpty = -> [emptyList():map(|a| -> a):isEmpty(), true]

function test_filter = ->
  [longLL(): filter(|a| -> (a % 2) == 0), lazyList(2, 4)]

function test_filterCopy = ->
  [longLL(): filter(|a| -> true), longLL()]

function test_filterEmpty = ->
  [emptyList(): filter(|a| -> true), emptyList()]

function test_range = {
  let r = |s, e| -> generator(
    |v| -> [v, v+1],
    |v| -> v >= e,
    s
  )
  return [
  [r(2, 5):asList(), r(0,1):asList(), r(1, 0)],
  [list[2, 3, 4], list[0], emptyList()]
]
}

function test_find = -> [
  [longLL():find(|n| -> n > 3), longLL():find(|v| -> false)],
  [4, null]
]

function test_join = -> [
  [lazyList(1, 2, 3): join(","), emptyList(): join(",")],
  ["1,2,3", ""]
]

function test_foldr = -> [lazyList(1, 2, 3): foldr(|a, b| -> a + b, 0), 6]

function test_foldrCopy = ->
  [longLL():foldr(|v, l| -> cons(v, l), emptyList()):asList(), longL()]

function test_foldrEmpty = -> [emptyList():foldr(|a, b| -> a + b, 0), 0]

function test_foldl = -> [lazyList(1, 2, 3): foldl(|a, b| -> a + b, 0), 6]

function test_foldlEmpty = -> [emptyList(): foldl(|a, b| -> a + b, 0), 0]

function test_take = -> [
  [longLL():take(3), longLL():take(42), emptyList(): take(2)],
  [lazyList(1, 2, 3), longLL(), emptyList()]
]

function test_takeWhile = -> [
  [longLL():takeWhile(|v| -> v < 4), longLL():takeWhile(|v| -> false),
   longLL():takeWhile(|v|->true)],
  [lazyList(1, 2, 3), emptyList(), longLL()]
]

function test_drop = -> [
  [longLL():drop(3), longLL():drop(0), longLL(): drop(42), emptyList():drop(3)],
  [lazyList(4, 5), longLL(), emptyList(), emptyList()]
]

function test_dropWhile = -> [
  [longLL():dropWhile(|v| -> v < 4): asList(),
   longLL():dropWhile(|v| -> false): asList(),
   longLL():dropWhile(|v| -> true): asList()],
  [list[4, 5], longL(), list[]]
]

local function fail = {
  throw AssertionError("Test should fail")
}

function test_destruct = {
  let a, b... = longLL()
  assertThat(a, `is(longLL(): head()))
  assertThat(b, `is(longLL(): tail()))

  let x, y, z... = longLL()
  assertThat(x, `is(longLL(): head()))
  assertThat(y, `is(longLL(): tail(): head()))
  assertThat(z, `is(longLL(): tail(): tail()))

  let c, d, e, f, g = longLL()
  assertThat(c, `is(1))
  assertThat(d, `is(2))
  assertThat(e, `is(3))
  assertThat(f, `is(4))
  assertThat(g, `is(5))

  let h, i, j, k, l... = longLL()
  assertThat(h, `is(1))
  assertThat(i, `is(2))
  assertThat(j, `is(3))
  assertThat(k, `is(4))
  assertThat(l: head(), `is(5))

  let c1, d1, e1, f1, g1, h1... = longLL()
  assertThat(c1, `is(1))
  assertThat(d1, `is(2))
  assertThat(e1, `is(3))
  assertThat(f1, `is(4))
  assertThat(g1, `is(5))
  assertThat(h1, `is(empty()))

  try {
    let v, w = longLL()
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }

  try {
    let m, n, o, p, q, r = longLL()
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_destruct_skip = {
  let s11, _, _, _, s12 = longLL()
  assertThat(s11, `is(1))
  assertThat(s12, `is(5))

  let s21, _, _, s22... = longLL()
  assertThat(s21, `is(1))
  assertThat(s22: head(), `is(4))
  assertThat(s22: tail():head(), `is(5))
  assertThat(s22: tail():tail(), `is(empty()))

  let _, s3, _... = longLL()
  assertThat(s3, `is(2))

}


function main = |args| {
  require(test_empty(), "err: test_empty")
  require(test_head(): get(0) == test_head(): get(1), "err: test_head")
  require(test_tail(): get(0) == test_tail(): get(1), "err: test_tail")
  require(test_tail_empty(): get(0) == test_tail_empty(): get(1), "err: test_tail_empty")
  require(test_head_tails(): get(0) == test_head_tails(): get(1), "err: test_head_tails")
  require(test_size(): get(0) == test_size(): get(1), "err: test_size")
  require(test_equals(): get(0) == test_equals(): get(1), "err: test_equals")
  require(test_from_iter(): get(0) == test_from_iter(): get(1), "err: test_from_iter")
  require(test_to_list(): get(0) == test_to_list(): get(1), "err: test_to_list")
  require(test_get(): get(0) == test_get(): get(1), "err: test_get")
  require(test_indexOf(): get(0) == test_indexOf(): get(1), "err: test_indexOf")
  require(test_contains(): get(0) == test_contains(): get(1), "err: test_contains")
  require(test_containsAll(): get(0) == test_containsAll(): get(1), "err: test_containsAll")
  require(test_iterable(): get(0) == test_iterable(): get(1), "err: test_iterable")
  require(test_constVar(): get(0) == test_constVar(): get(1), "err: test_constVar")
  require(test_cons(): get(0) == test_cons(): get(1), "err: test_cons")
  require(test_map(): get(0) == test_map(): get(1), "err: test_map")
  require(test_mapEmpty(): get(0) == test_mapEmpty(): get(1), "err: test_mapEmpty")
  require(test_filter(): get(0) == test_filter(): get(1), "err: test_filter")
  require(test_filterCopy(): get(0) == test_filterCopy(): get(1), "err: test_filterCopy")
  require(test_filterEmpty(): get(0) == test_filterEmpty(): get(1), "err: test_filterEmpty")
  require(test_range(): get(0) == test_range(): get(1), "err: test_range")
  require(test_find(): get(0) == test_find(): get(1), "err: test_find")
  require(test_join(): get(0) == test_join(): get(1), "err: test_join")
  require(test_foldr(): get(0) == test_foldr(): get(1), "err: test_foldr")
  require(test_foldrCopy(): get(0) == test_foldrCopy(): get(1), "err: test_foldrCopy")
  require(test_foldrEmpty(): get(0) == test_foldrEmpty(): get(1), "err: test_foldrEmpty")
  require(test_foldl(): get(0) == test_foldl(): get(1), "err: test_foldl")
  require(test_foldlEmpty(): get(0) == test_foldlEmpty(): get(1), "err: test_foldlEmpty")
  require(test_take(): get(0) == test_take(): get(1), "err: test_take")
  require(test_takeWhile(): get(0) == test_takeWhile(): get(1), "err: test_takeWhile")
  require(test_drop(): get(0) == test_drop(): get(1), "err: test_drop")
  require(test_dropWhile(): get(0) == test_dropWhile(): get(1), "err: test_dropWhile")
  test_destruct()
}
