
module golotest.execution.Destructuring

struct Point = { x, y }

function test_tuple_samesize = {
  let a, b, c = [1, 2, 3]
  require(a == 1, "err")
  require(b == 2, "err")
  require(c == 3, "err")
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
  let fst, scd, rest... = [1, 2, 3, 4, 5]
  require(fst == 1, "err")
  require(scd == 2, "err")
  require(rest == [3, 4, 5], "err")
}

function test_tuple_less = {
  let fst, scd = [1, 2, 3, 4]
  require(fst == 1, "err")
  require(scd == 2, "err")
}

function test_struct = {
  let p = Point(3, 4)
  let x, y = p
  require(x == 3, "err")
  require(y == 4, "err")
}

function test_list = {
  let l = list[1, 2, 3, 4, 5]

  let fst, scd, rest... = l
  require(fst == 1, "err")
  require(scd == 2, "err")
  require(rest == [3, 4, 5], "err")

}

function test_array = {
  let fst, scd, rest... = array[1, 2, 3, 4, 5]
  require(fst == 1, "err")
  require(scd == 2, "err")
  require(rest == [3, 4, 5], "err")
}

function test_range = {
  let fst, scd, rest... = [1..6]
  require(fst == 1, "err")
  require(scd == 2, "err")
  require(rest == [3, 4, 5], "err")

  let a, b = [1..4]
  require(a == 1, "err")
  require(b == 2, "err")
}

function test_foreach = {
  let l = [ [1, 2, 3], [3, 4, 5] ]
  var i = 0
  foreach a, b in l {
    require(a == l: get(i): get(0), "err")
    require(b == l: get(i): get(1), "err")
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

function main = |args| {
  test_tuple_samesize()
  test_tuple_rest()
  test_tuple_less()
  test_tuple_var()
  test_struct()
  test_list()
  test_range()
  test_foreach()
  test_map()
  test_swap()
  println("ok")
}
