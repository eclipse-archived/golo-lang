module golo.test.LazyList

import gololang.lazylist

local function longLL = ->
      LazyList(1, -> LazyList(2, -> LazyList(3, -> LazyList(4, -> LazyList(5, -> Empty())))))

local function longL = -> list[1, 2, 3, 4, 5]

#========================================================
function test_empty = -> (
  Empty():isEmpty()
  and (Empty() is Empty())
  and lazyList() is Empty()
)

function test_head = {
  let ll = LazyList(1, -> Empty())
  return [ll: head(), 1]
}

function test_tail = {
  let ll = LazyList(1, -> LazyList(2, -> Empty()))
  return [ll:tail():head(), 2]
}

function test_tail_empty = {
  let ll = LazyList(1, -> Empty())
  return [ll:tail():isEmpty(), true]
}

function test_head_tails = {
  let ll = longLL()
  return [
    [ll:head(), ll:tail():head(), ll:tail():tail():head(),
     ll:tail():tail():tail():head(),
     ll:tail():tail():tail():tail():head(),
     ll:tail():tail():tail():tail():tail()],
    [1, 2, 3, 4, 5, Empty()]
  ]
}

function test_size = -> [longLL(): size(), 5]

function test_equals = -> [
  [longLL(), longLL()],
  [longLL(), lazyList(1, 2, 3, 4, 5)]
]

function test_from_iter = -> [fromIter(longL()), longLL()]

#    Skip(Test("infinite gen to LList")({
#      let ll = ittoLazyList(gololang.iterutils.count())
#      assertEquals(ll:tail():tail():tail():tail():head(), 4)
#    })),

function test_to_list = -> [
  [longLL(): asList(), Empty(): asList()],
  [longL(), list[]]
]


#    Test("convert to array")({
#      assertEquals(longLL():toArray():asList(), longL())
#    }),

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
  cons(1, cons(2, cons(3, cons(4, cons(5, Empty()))))),
  longLL()
]

function test_map = {
  let res = LazyList(2, -> LazyList(4, -> LazyList(6, -> LazyList(8, -> LazyList(10, -> Empty())))))
  return [longLL():map(|a| -> 2* a), res]
}

function test_mapEmpty = -> [Empty():map(|a| -> a):isEmpty(), true]

function test_foldr = -> [lazyList(1, 2, 3): foldr(|a, b| -> a + b, 0), 6]

function test_foldrCopy = ->
  [longLL():foldr(|v, l| -> cons(v, l), Empty()):asList(), longL()]

function test_foldrEmpty = -> [Empty():foldr(|a, b| -> a + b, 0), 0]

function test_foldl = -> [lazyList(1, 2, 3): foldl(|a, b| -> a + b, 0), 6]

function test_foldlEmpty = -> [Empty(): foldl(|a, b| -> a + b, 0), 0]

function test_filter = ->
  [longLL(): filter(|a| -> (a % 2) == 0), lazyList(2, 4)]

function test_filterCopy = ->
  [longLL(): filter(|a| -> true), longLL()]

function test_filterEmpty = ->
  [Empty(): filter(|a| -> true), Empty()]

function test_range = -> [
  [r(2, 5):asList(), r(0,1):asList(), r(1, 0)],
  [list[2, 3, 4], list[0], Empty()]
]

function test_zip = {
  let l1 = lazyList(1, 2, 3, 4)
  let l2 = lazyList('a', 'b', 'c')
  let l3 = lazyList(5, 6)
  return [zip(list[l1, l2, l3]), lazyList(
    [1, 'a', 5],
    [2, 'b', 6]
  )]
}

function test_zipMeth = {
  let l1 = lazyList(1, 2, 3, 4)
  let l2 = list['a', 'b', 'c']
  let l3 = lazyList(5, 6)
  return [l1:zip(l2, l3):asList(), list[[1, 'a', 5], [2, 'b', 6]]]
}

function test_enumerate = ->
  [longLL(): enumerate(), lazyList([1, 0], [2, 1], [3, 2], [4, 3], [5, 4])]

function test_take = -> [
  [longLL():take(3), longLL():take(42), Empty(): take(2)],
  [lazyList(1, 2, 3), longLL(), Empty()]
]

function test_takeWhile = -> [
  [longLL():takeWhile(|v| -> v < 4), longLL():takeWhile(|v| -> false),
   longLL():takeWhile(|v|->true)],
  [lazyList(1, 2, 3), Empty(), longLL()]
]

function test_drop = -> [
  [longLL():drop(3), longLL():drop(0), longLL(): drop(42), Empty():drop(3)],
  [lazyList(4, 5), longLL(), Empty(), Empty()]
]

function test_dropWhile = -> [
  [longLL():dropWhile(|v| -> v < 4): asList(),
   longLL():dropWhile(|v| -> false): asList(),
   longLL():dropWhile(|v| -> true): asList()],
  [list[4, 5], longL(), list[]]
]

function test_find = -> [
  [longLL():find(|n| -> n > 3), longLL():find(|v| -> false)],
  [4, null]
]

function test_join = -> [
  [lazyList(1, 2, 3): join(","), Empty(): join(",")],
  ["1,2,3", ""]
]

function test_count = -> [
  [count(2): take(5): asList(), count(): take(2): asList()],
  [list[2, 3, 4, 5, 6], list[0, 1]]
]
