module golo.test.LazyList

import gololang.lazylist

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

