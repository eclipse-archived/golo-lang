
module golotest.execution.Destructuring

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

import org.eclipse.golo.runtime

struct Point = { x, y }

struct Triplet = {a, b, c}

local function fail = {
  throw AssertionError("Test should fail")
}

function test_tuple_samesize = {
  let a, b, c = [1, 2, 3]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(3))
}

function test_tuple_samesize_with_sub = {
  let a, b, c... = [1, 2, 3]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is([3]))
}

function test_tuple_var = {
  var a, b, c = [1, 2, 3]
  require(a == 1, "err")
  require(b == 2, "err")
  require(c == 3, "err")
  a = 4
  b = 5
  c = 6
  require(a == 4, "err")
  require(b == 5, "err")
  require(c == 6, "err")
}

function test_tuple_rest = {
  let a, b, c... = [1, 2, 3, 4, 5]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is([3, 4, 5]))
}

# ignored, old version. Should fail now.
function _tuple_less_old = {
  let fst, scd = [1, 2, 3, 4]
  require(fst == 1, "err")
  require(scd == 2, "err")
}

function test_tuple_less_new = {
  try {
    let fst, scd = [1, 2, 3, 4]
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_tuple_more_with_sub = {
  let a, b, c... = [1, 2]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is([]))
}

function test_tuple_more = {
  try {
    let a, b, c = [1, 2]
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_tuple_many_more_with_sub = {
  try {
    let a, b, c, d... = [1, 2]
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_tuple_many_more = {
  try {
    let a, b, c, d = [1, 2]
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}


function test_struct = {
  let p = Point(3, 4)
  let x, y = p
  require(x == 3, "err")
  require(y == 4, "err")

  let a, b, c = Triplet(5, 6, 7)
  require(a == 5, "err")
  require(b == 6, "err")
  require(c == 7, "err")
}

function test_struct_not_exact = {
  try {
    let x, y = Triplet(1, 2, 3)
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }

  try {
    let a, b, c, d = Triplet(4, 5, 6)
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }

  try {
    let a, b, c... = Triplet(4, 5, 6)
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_list = {
  let l = list[1, 2, 3, 4, 5]

  let fst, scd, rest... = l
  require(fst == 1, "err")
  require(scd == 2, "err")
  require(rest == list[3, 4, 5], "err")

}

function test_array_less = {
  try {
    let a, b = array[1, 2, 3, 4]
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_array_less_with_sub = {
  let fst, scd, rest... = array[1, 2, 3, 4, 5]
  assertThat(fst, `is(1))
  assertThat(scd, `is(2))
  assertThat(rest, `is(arrayContaining(3, 4, 5)))
}

function test_array_exact = {
  let a, b, c = array[1, 2, 3]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(3))
}

function test_array_exact_with_sub = {
  let a, b, c... = array[1, 2, 3]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(arrayContaining(3)))
}

function test_array_more = {
  try {
    let a, b, c = array[1, 2]
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }

  try {
    let a, b, c, d... = array[1, 2]
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_array_more_with_sub = {
  let a, b, c... = array[1, 2]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(emptyArray()))
}

function test_range_1 = {
  let f, s, r... = [1..6]
  assertThat(f, `is(1))
  assertThat(s, `is(2))
  assertThat(r, `is([3..6]))
}

function test_range_2 = {
  let x, y, z = [0..3]
  assertThat(x, `is(0))
  assertThat(y, `is(1))
  assertThat(z, `is(2))
}

function test_range_3 = {
  let c, d, e... = [0..3]
  assertThat(c, `is(0))
  assertThat(d, `is(1))
  assertThat(e, `is([2..3]))
}

function test_range_4 = {
  let g, h, i, j... = [0..3]
  assertThat(g, `is(0))
  assertThat(h, `is(1))
  assertThat(i, `is(2))
  assertThat(j, `is(emptyIterable()))
}

function test_range_5 = {
  try {
    let a, b = [1..4]
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_range_6 = {
  try {
    let k, l, m, n = [0..3]
    fail()
  } catch(e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_foreach = {
  let l = list[ [1, 2, 3], [3, 4, 5] ]
  var i = 0
  foreach a, b, c in l {
    require(a == l: get(i): get(0), "err")
    require(b == l: get(i): get(1), "err")
    require(c == l: get(i): get(2), "err")
    i = i + 1
  }

  i = 0
  foreach a, b... in l {
    require(a == l: get(i): head(), "err")
    require(b == l: get(i): tail(), "err")
    i = i + 1
  }
}

function test_map = {
  let m = map[ ["k", 1] ]
  foreach k, v in m: entrySet() {
    require(k == "k", "err")
    require(v == 1, "err")
  }
}

union MyUnion = {
  Foo = { x, y }
  Bar = { a, b, c }
  Void
}

function test_union = {
  let foo = MyUnion.Foo(1, 2)
  let x, y = foo
  require(x == 1, "err")
  require(y == 2, "err")

  let bar = MyUnion.Bar("a", "b", "c")
  let a, b, c = bar
  require(a == "a", "err")
  require(b == "b", "err")
  require(c == "c", "err")
}

function test_union_bad = {
  try {
    let a, b, c = MyUnion.Foo(1, 2)
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }

  try {
    let a, b, c... = MyUnion.Foo(1, 2)
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }

  try {
    let a, b, c... = MyUnion.Bar(1, 2, 3)
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }

  try {
    let a, b = MyUnion.Bar(1, 2, 3)
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }

  try {
    let a, b = MyUnion.Void()
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_swap = {
  var a, b = [1, 2]
  require(a == 1, "err")
  require(b == 2, "err")

  a, b = [b, a]
  require(a == 2, "err")
  require(b == 1, "err")

  var c = 0
  a, b, c = [c, a, b]
  require(a == 0, "err")
  require(b == 2, "err")
  require(c == 1, "err")
}

# issue #391
function test_destruct_affectation_inside_closure = {
  let closure = {
    let a, b = [1,2]
    return a
  }
  require(closure() == 1, "err")
}

