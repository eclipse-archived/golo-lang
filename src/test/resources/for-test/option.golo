module golotest.error.Option

import gololang.error.Errors

local function assertEquals = |value, expected| {
  require(value == expected, 
    String.format("expected %s, got %s", expected, value))
}
# ............................................................................ #

function test_testingmethods = {
  let s = Some(42)
  let n = None()
  assertEquals(s: isNone(), false)
  assertEquals(n: isNone(), true)
  assertEquals(s: isSome(), true)
  assertEquals(n: isSome(), false)
  assertEquals(s: isSome(42), true)
  assertEquals(s: isSome("42"), false)
  assertEquals(n: isSome(42), false)
}

function test_toList = {
  assertEquals(Some(42): toList(), list[42])
  assertEquals(None(): toList(), list[])
}

function test_iterator = {
  var l = list[]
  foreach v in Some(42) {
    l: add(v)
  }
  assertEquals(l, list[42])
  l = list[]
  foreach v in None() {
    l: add(v)
  }
  assertEquals(l, list[])
}

function test_and = {
  let s1 = Some(1)
  let s2 = Some("2")
  let n = None()

  assertEquals(s1:`and(n), None())
  assertEquals(n:`and(s1), None())
  assertEquals(n:`and(n), None())
  assertEquals(s1:`and(s2), Some("2"))
}

function test_or = {
  let s1 = Some(1)
  let s2 = Some("2")
  let n = None()

  assertEquals(s1:`or(n), Some(1))
  assertEquals(n:`or(s1), Some(1))
  assertEquals(n:`or(n), None())
  assertEquals(s1:`or(s2), Some(1))
}

function test_andThen = {
  let plus2 = |x| -> Some(x + 2)
  assertEquals(Some(38): andThen(plus2): andThen(plus2), Some(42))
  assertEquals(None(): andThen(plus2): andThen(plus2), None())
}

function test_orElseGet = {
  assertEquals(Some(42): orElseGet(-> "foo"), 42)
  assertEquals(None(): orElseGet(-> "foo"), "foo")
}

function test_applicative = {
  assertEquals(Some(|x| -> x + 10): apply(Some(32)), Some(42))
  assertEquals(Some(|x| -> x: toUpperCase()): apply(Some("foo")), Some("FOO"))
  assertEquals(None(): apply(Some(42)), None())
  assertEquals(Some(|x| -> x + 1): apply(None()), None())

  assertEquals(
    Some(|a, b| -> a + b)
    : apply(Some(21))
    : apply(Some(21)),
    Some(42))
}

function test_toResult = {
  assertEquals(Some(42): toResult("err"), gololang.error.Result.ok(42))
  assertEquals(None(): toResult("err"), gololang.error.Result.fail("err"))
  
  let err = IllegalArgumentException("err")
  assertEquals(Some(42): toResult(err), gololang.error.Result.ok(42))
  assertEquals(None(): toResult(err), gololang.error.Result.error(err))

}

# ............................................................................ #
function main = |args| {
  test_testingmethods()
  test_toList()
  test_iterator()
  test_and()
  test_or()
  test_andThen()
  test_orElseGet()
  test_applicative()
  test_toResult()
  println("ok")
}
