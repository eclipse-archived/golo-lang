
module golotest.execution.Comprehension

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

----
simple comprehension with a `for` loop.
----
function test_forloop = {
#tag::for_loop[]
  let l = list[ 2 * x for (var x = 1, x < 5, x = x + 1) ]
#end::for_loop[]
  assertThat(l, contains(2, 4, 6, 8))
}

----
simple comprehension with a `foreach` loop on a literal.
----
function test_foreachloop = {
  let l = list[ 2 * x foreach x in [1..5] ]
  assertThat(l, contains(2, 4, 6, 8))
}

----
simple comprehension with a `foreach` loop on a reference.
----
function test_foreach_onref = {
  let a = [1..5]
  let l = list[ 2 * x foreach x in a ]
  assertThat(l, contains(2, 4, 6, 8))
}

----
comprehension with `foreach` and `when` to filter
----
function test_foreach_when = {
  let l = list[ x / 2 foreach x in [1..10] when (x % 2) == 0 ]
  assertThat(l, contains(1, 2, 3, 4))
}

----
comprehension with `foreach` on a reference and `when` to filter
----
function test_foreach_when_ref = {
  # tag::present[]
  let data = list[1, 2, 3, 4, 5, 6]
  let transformed = list[x * 2 foreach x in data when (x % 2) == 0]
  # end::present[]
  assertThat(transformed, contains(4, 8, 12))
  # tag::mapfilter[]
  let transformed2 = data: filter(|x| -> (x % 2) == 0): map(|x| -> x * 2)
  # end::mapfilter[]
  assertThat(transformed2, contains(4, 8, 12))
}

----
comprehension on vector
----
function test_vector = {
  let v = vector[ 2 * x foreach x in [1..5] ]
  assertThat(v, contains(2, 4, 6, 8))
}

----
comprehension on array
----
function test_array = {
  let v = array[ 2 * x foreach x in [1..5] ]
  assertThat(v, arrayContaining(2, 4, 6, 8))
}

----
comprehension on tuples, with both notations
----
function test_tuple = {
  let v = tuple[ 2 * x foreach x in [1..5] ]
  let t = [ 2 * x foreach x in [1..5] ]
  assertThat(v, `is(tuple[2, 4, 6, 8]))
  assertThat(t, `is([2, 4, 6, 8]))
}

----
comprehension on set
----
function test_set = {
  let l = list[1, 2, 3, 1, 2, 3, 1, 2, 3]
  let s = set[ x * 2 foreach x in l ]
  assertThat(s, containsInAnyOrder(2, 4, 6))
}

function test_set_two = {
#tag::set_two[]
  let dices = set[ x + y foreach x in [1..7] foreach y in [1..7]]
  assertThat(dices, containsInAnyOrder(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12))
#end::set_two[]
}

function test_map = {
  let target = map[
    ["k1", 1],
    ["k2", 2],
    ["k3", 3],
    ["k4", 4]
  ]
  let m1 = map[ mapEntry("k" + i, i) foreach i in [1..5] ]
#tag::map_comprehension[]
  let myMap = map[ ["k" + i, i] foreach i in [1..5] ]
#end::map_comprehension[]
  assertThat(m1, equalTo(target))
  assertThat(myMap, equalTo(target))
}

----
Two nested loops, with different sizes.
----
function test_two_loops = {
#tag::two_loops[]
  let l = list[ [x, y] foreach x in [0..5] foreach y in list["a", "b", "c"] ]
#end::two_loops[]
  assertThat(l, equalTo(_two_ref()))
}

local function _two_ref = {
#tag::nested_loops[]
  let l = list[]
  foreach x in [0..5] {
    foreach y in list["a", "b", "c"] {
      l: add([x, y])
    }
  }
#end::nested_loops[]
  return l
}

function test_two_mixed_loops = {
  let ref= list[
    [0, 0],
    [0, 1],
    [0, 2],
    [1, 0],
    [1, 1],
    [1, 2],
    [2, 0],
    [2, 1],
    [2, 2]
  ]
  let l1 = list[ [x, y] foreach x in [0..3] for (var y = 0, y < 3, y = y + 1) ]
  let l2 = list[ [x, y] for (var x = 0, x < 3, x = x + 1) foreach y in [0..3] ]
  assertThat(l1, equalTo(ref))
  assertThat(l2, equalTo(ref))
}

function test_more_loops = {
  let l = list[ [x,y,z] foreach x in [0,1,2] when (x % 2) == 1
                        foreach y in [1,2,3] when y < 2
                        foreach z in [1] ]
  assertThat(l, contains([1,1,1]))
}

function test_destruct = {
#tag::destruct[]
  let couples = list[ [1, 2], [2, 3], [3, 4] ]
  let sums = list[ a + b foreach a, b in couples ]
#end::destruct[]
  assertThat(sums, contains(3, 5, 7))
}

function test_filtered_destruct_two = {
  let aMap = map[
    ["foo", 5],
    ["bar", 45],
    ["plop", 10]
  ]
#tag::nested_destruct[]
  let l = list[
  "%s: %s": format(k, x * v)
  foreach x in [1..10]
    when (x % 2) == 1
  foreach k, v in aMap:entrySet()
    when k: startsWith("f") or v >= 42
  ]
#end::nested_destruct[]

  assertThat(l, contains(
    "foo: 5", "bar: 45",
    "foo: 15", "bar: 135",
    "foo: 25", "bar: 225",
    "foo: 35", "bar: 315",
    "foo: 45", "bar: 405"
  ))
}

function test_map_destruct = {
  let base = map[
    ["a", 1],
    ["b", 2],
    ["c", 3]
  ]
  let m = map[ [k, v] foreach k, v in base: entrySet() ]
  assertThat(m, equalTo(base))
}
