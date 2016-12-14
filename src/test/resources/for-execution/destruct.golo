
module golotest.execution.Destructuring

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

import org.eclipse.golo.runtime

#tag::Point[]
struct Point = { x, y }
#end::Point[]

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

function test_tuple_skip_1 = {
  let a, _, _, b = tuple[1, 2, 3, 4]
  assertThat(a, `is(1))
  assertThat(b, `is(4))
}

function test_tuple_skip_2 = {
  let a, _, _, c... = tuple[1, 2, 3, 4, 5]
  assertThat(a, `is(1))
  assertThat(c, `is(tuple[4, 5]))
}

function test_tuple_skip_3 = {
  let _, b, _... = tuple[1, 2, 3, 4, 5]
  assertThat(b, `is(2))
}


function test_struct = {
#tag::testPoint[]
  let p = Point(3, 4)
  let x, y = p
  require(x == 3, "err")
  require(y == 4, "err")
#end::testPoint[]

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

function test_list_1 = {
  let a, b, c... = list[1, 2, 3, 4, 5]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(list[3, 4, 5]))
}

function test_list_2 = {
  let a, b, c... = list[1, 2, 3]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(list[3]))
}

function test_list_3 = {
  let a, b, c = list[1, 2, 3]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(3))
}

function test_list_4 = {
  let a, b, c, d... = list[1, 2, 3]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(3))
  assertThat(d, `is(list[]))
}

function test_list_5 = {
  try {
    let a, b = list[1, 2, 3]
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_list_6 = {
  try {
    let a, b, c = list[1, 2]
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_list_7 = {
  try {
    let a, b, c, d... = list[1, 2]
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_list_skip_1 = {
  let a, _, _, b = list[1, 2, 3, 4]
  assertThat(a, `is(1))
  assertThat(b, `is(4))
}

function test_list_skip_2 = {
  let a, _, _, c... = list[1, 2, 3, 4, 5]
  assertThat(a, `is(1))
  assertThat(c, `is(list[4, 5]))
}

function test_list_skip_3 = {
  let _, b, _... = list[1, 2, 3, 4, 5]
  assertThat(b, `is(2))
}


function test_iter_1 = {
  let a, b, c... = list[1, 2, 3, 4, 5]: iterator()
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, isA(java.util.Iterator.class))
  assertThat(c: next(), `is(3))
  assertThat(c: next(), `is(4))
  assertThat(c: next(), `is(5))
  assertThat(c: hasNext(), `is(false))
}

function test_iter_2 = {
  let a, b, c... = list[1, 2, 3]: iterator()
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, isA(java.util.Iterator.class))
  assertThat(c: next(), `is(3))
  assertThat(c: hasNext(), `is(false))
}

function test_iter_3 = {
  let a, b, c = list[1, 2, 3]: iterator()
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(3))
}

function test_iter_4 = {
  let a, b, c, d... = list[1, 2, 3]: iterator()
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(3))
  assertThat(d, isA(java.util.Iterator.class))
  assertThat(d: hasNext(), `is(false))
}

function test_iter_5 = {
  try {
    let a, b = list[1, 2, 3]: iterator()
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_iter_6 = {
  try {
    let a, b, c = list[1, 2]: iterator()
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_iter_7 = {
  try {
    let a, b, c, d... = list[1, 2]:iterator()
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_iter_skip_1 = {
  let a, _, _, b = list[1, 2, 3, 4]:iterator()
  assertThat(a, `is(1))
  assertThat(b, `is(4))
}

function test_iter_skip_2 = {
  let a, _, _, c... = list[1, 2, 3, 4, 5]: iterator()
  assertThat(a, `is(1))
  assertThat(c: next(), `is(4))
  assertThat(c: next(), `is(5))
  assertThat(c: hasNext(), `is(false))
}

function test_iter_skip_3 = {
  let _, b, _... = list[1, 2, 3, 4, 5]: iterator()
  assertThat(b, `is(2))
}

function test_iterable_1 = {
  let a, b, c... = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2, 3, 4, 5]: iterator())
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, isA(java.lang.Iterable.class))
  assertThat(c, contains(3, 4, 5))
}

function test_iterable_2 = {
  let a, b, c... = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2, 3]: iterator())
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, isA(java.lang.Iterable.class))
  assertThat(c, contains(3))
}

function test_iterable_3 = {
  let a, b, c = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2, 3]: iterator())
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(3))
}

function test_iterable_4 = {
  let a, b, c, d... = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2, 3]: iterator())
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(3))
  assertThat(d, isA(java.lang.Iterable.class))
  assertThat(d, `is(emptyIterable()))
}

function test_iterable_5 = {
  try {
    let a, b = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2, 3]: iterator())
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_iterable_6 = {
  try {
    let a, b, c = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2]: iterator())
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_iterable_7 = {
  try {
    let a, b, c, d... = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2]:iterator())
    fail()
  } catch (e) {
    assertThat(e, isA(InvalidDestructuringException.class))
  }
}

function test_iterable_skip_1 = {
  let a, _, _, b = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2, 3, 4]:iterator())
  assertThat(a, `is(1))
  assertThat(b, `is(4))
}

function test_iterable_skip_2 = {
  let a, _, _, c... = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2, 3, 4, 5]: iterator())
  assertThat(a, `is(1))
  assertThat(c, contains(4, 5))
}

function test_iterable_skip_3 = {
  let _, b, _... = asInterfaceInstance(java.lang.Iterable.class, -> list[1, 2, 3, 4, 5]: iterator())
  assertThat(b, `is(2))
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

function test_array_exact_skip = {
  let a, _, _, b = array[1, 2, 3, 4]
  assertThat(a, `is(1))
  assertThat(b, `is(4))
}

function test_array_exact_with_sub = {
  let a, b, c... = array[1, 2, 3]
  assertThat(a, `is(1))
  assertThat(b, `is(2))
  assertThat(c, `is(arrayContaining(3)))
}

function test_array_with_sub_skip = {
  let a, _, _, c... = array[1, 2, 3, 4, 5]
  assertThat(a, `is(1))
  assertThat(c, `is(arrayContaining(4, 5)))
}

function test_array_with_sub_skip2 = {
  let _, b, _... = array[1, 2, 3, 4, 5]
  assertThat(b, `is(2))
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

function test_range_skip_1 = {
  let a, _, _, b = [1..5]
  assertThat(a, `is(1))
  assertThat(b, `is(4))
}

function test_range_skip_2 = {
  let a, _, _, c... = [1..6]
  assertThat(a, `is(1))
  assertThat(c, `is([4..6]))
}

function test_range_skip_3 = {
  let _, b, _... = [1..6]
  assertThat(b, `is(2))
}



function test_foreach = {
  let l = list[ [1, 2, 3], [3, 4, 5] ]
  var i = 0
  foreach a, b, c in l {
    assertThat(a, `is(l: get(i): get(0)))
    assertThat(b, `is(l: get(i): get(1)))
    assertThat(c, `is(l: get(i): get(2)))
    i = i + 1
  }

  i = 0
  foreach a, b... in l {
    assertThat(a, `is(l: get(i): head()))
    assertThat(b, `is(l: get(i): tail()))
    i = i + 1
  }

  i = 0
  foreach _, b, _ in l {
    assertThat(b, `is(l: get(i): get(1)))
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

function main = |args| {
  test_iterable_1()
}
