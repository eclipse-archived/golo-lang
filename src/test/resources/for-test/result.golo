
module golotest.error.Result

import gololang.error
import gololang.Errors

local function assertEquals = |value, expected| {
  require(value == expected,
    String.format("expected %s, got %s", expected, value))
}

local function assertTrue = |value| {
  assertEquals(value, true)
}

local function canFail = |b| -> match {
  when b is null then null
  when b oftype Boolean.class and b then raise("err")
  otherwise b
}

# ........................................................................... #
function test_and = {
  let ok1 = Result.ok(1)
  let ok2 = Result.ok("2")
  let err1 = Result.fail("err1")
  let err2 = Result.fail("err2")
  let nop = Result.empty()

  assertEquals(ok1:`and(err1), Result.fail("err1"))
  assertEquals(ok1:`and(ok2), Result.ok("2"))
  assertEquals(ok1:`and(nop), Result.empty())

  assertEquals(err1:`and(ok1), Result.fail("err1"))
  assertEquals(err1:`and(err2), Result.fail("err1"))
  assertEquals(err1:`and(nop), Result.fail("err1"))

  assertEquals(nop:`and(ok1), Result.ok(1))
  assertEquals(nop:`and(err1), Result.fail("err1"))
  assertEquals(nop:`and(nop), Result.empty())
}


function test_or = {
  let ok1 = Result.ok(1)
  let ok2 = Result.ok(2)
  let err1 = Result.fail("err1")
  let err2 = Result.fail("err2")
  let nop = Result.empty()

  assertEquals(ok1:`or(err1), Result.ok(1))
  assertEquals(ok1:`or(ok2), Result.ok(1))
  assertEquals(ok1:`or(nop), Result.ok(1))

  assertEquals(err1:`or(ok1), Result.ok(1))
  assertEquals(err1:`or(err2), Result.fail("err2"))
  assertEquals(err1:`or(nop), Result.empty())

  # XXX: check semantic empty or ok(1)?
  assertEquals(nop:`or(ok1), Result.empty())
  assertEquals(nop:`or(err1), Result.empty())
  assertEquals(nop:`or(nop), Result.empty())
}

function test_reduce = {
  assertEquals(Result.ok("b"): reduce("a", |x, y| -> x + y), "ab")
  assertEquals(Result.empty(): reduce(42, |x, y| -> x + y), 42)
  assertEquals(Result.fail("error"): reduce(42, |x, y| -> x + y), 42)
}

function test_flatten = {
  assertEquals(Ok(Ok(42)): flattened(), Ok(42))
  assertEquals(Error("err"): flattened(), Error("err"))
  assertEquals(Ok(Error("err")): flattened(), Error("err"))

  try {
    Ok(42): flattened()
  } catch (e) {
    require(e oftype java.lang.ClassCastException.class, "Bad Exception")
  }

}

function test_result = {
  let failer = result(^canFail)
  assertEquals(failer(null), Result.empty())
  assertEquals(failer(true), Result.fail("err"))
  assertEquals(failer(42), Result.ok(42))
}

function test_closure_map = {
  let f = |x| -> match {
    when x is null then Result.fail("was null")
    otherwise Result.ok(x)
  }
  assertEquals(f(40): map(|x| -> x + 2), Result.ok(42))
  assertEquals(f(null): map(|x| -> x + 2), Result.fail("was null"))
}

function test_flatMap = {
  let plus2 = |x| -> Result.ok(x + 2)
  assertEquals(Result.ok(38): flatMap(plus2): flatMap(plus2),
    Result.ok(42))
  assertEquals(Result.ok(null): flatMap(plus2): flatMap(plus2),
    Result.empty())
  assertEquals(Result.fail("err"): flatMap(plus2): flatMap(plus2),
    Result.fail("err"))
}

function test_andThen = {
  let plus2 = |x| -> x + 2
  let mult2 = |x| -> Result.ok(x * 2)
  assertEquals(Result.ok(19): andThen(plus2): andThen(mult2),
    Result.ok(42))
  assertEquals(Result.ok(20): andThen(mult2): andThen(plus2),
    Result.ok(42))
  assertEquals(Result.ok(null): andThen(plus2): andThen(mult2),
    Result.empty())
  assertEquals(Result.ok(null): andThen(mult2): andThen(plus2),
    Result.empty())
  assertEquals(Result.fail("err"): andThen(plus2): andThen(mult2),
    Result.fail("err"))
  assertEquals(Result.fail("err"): andThen(mult2): andThen(plus2),
    Result.fail("err"))
}

function test_either = {
  let twice = |x| -> 2 * x
  let recover = |err| -> "err"
  let answer = -> "default"
  assertEquals(Ok(21): either(twice, recover), 42)
  assertEquals(Error("err"): either(twice, recover), "err")
  assertEquals(Ok(null): either(twice, recover, answer), "default")
}

function test_orElseGet = {
  assertEquals(Result.ok(42): orElseGet(-> "foo"), 42)
  assertEquals(Result.fail("err"): orElseGet(-> "foo"), "foo")
  assertEquals(Result.empty(): orElseGet(-> "bar"), "bar")
}

function test_applicative = {
  let f = Result.ok(|x| -> x + 10)
  assertEquals(f: apply(Result.ok(32)), Result.ok(42))
  assertEquals(f: apply(Result.empty()), Result.empty())
  assertEquals(f: apply(Result.fail("err")), Result.fail("err"))

  assertEquals(Result.empty(): apply(Result.ok(32)), Result.empty())
  assertEquals(Result.fail("err"): apply(Result.ok(32)), Result.fail("err"))

  assertEquals(Result.ok(|x, y| -> x + y)
      : apply(Result.ok(21))
      : apply(Result.ok(21)),
    Result.ok(42))
}

function test_trying = {
  let success = trying({
    return 42
  })
  assertEquals(success, Result.ok(42))

  let failed = trying({
    raise("err")
  })
  assertEquals(failed, Result.fail("err"))

  let empty = trying({})
  assertEquals(empty, Result.empty())
}

@raising
local function willRaise = |x| -> match {
  when x is null then Result.empty()
  when x then Result.fail("err")
  otherwise Result.ok(42)
}

function test_raising = {
  assertEquals(willRaise(false), 42)
  try {
    willRaise(null)
  } catch (e) {
    require(e oftype java.util.NoSuchElementException.class,
      "NoSuchElementException expected")
    assertEquals(e: getMessage(), "Empty result")
  }
  try {
    willRaise(true)
  } catch (e) {
    require(e oftype java.lang.RuntimeException.class,
      "RuntimeException expected")
    assertEquals(e: getMessage(), "err")
  }
}

function test_catching = {
  let foo = |x| -> x: toUpperCase()
  let safeFoo = catching("plop")(foo)

  assertEquals(safeFoo(null), "plop")
  assertEquals(safeFoo(None()), "plop")
  assertEquals(safeFoo(Error("err")), "plop")
  assertEquals(safeFoo(Ok("a")), "A")
  assertEquals(safeFoo(Some("a")), "A")

  let errFoo = catching(^gololang.error.Result::error)(foo)
  assertTrue(errFoo(null): isError())
  assertTrue(errFoo(None()): isError())
  assertEquals(errFoo(Some("a")), "A")
  assertEquals(errFoo(Ok("a")), "A")
}


@result
function impParsing = |a, b, c| {
  let x = intValue(a)
  let y = intValue(b)
  let z = intValue(c)
  return x + y + z
}

function test_imperative = {
  assertEquals(impParsing("11", "11", "20"), Ok(42))

  assertEquals(impParsing("l", "21", "1"),
               Result(NumberFormatException("For input string: \"l\"")))
  assertEquals(impParsing("21", "m", "r"),
               Result(NumberFormatException("For input string: \"m\"")))
}

function parseResult = |v| -> trying(-> intValue(v))

function monadicParsing = |a, b, c| ->
    parseResult(a): andThen(|x| ->
    parseResult(b): andThen(|y| ->
    parseResult(c): andThen(|z| ->
    Ok(x + y + z))))


function test_monadic = {
  assertEquals(monadicParsing("11", "11", "20"), Ok(42))

  assertEquals(monadicParsing("l", "21", "1"),
               Result(NumberFormatException("For input string: \"l\"")))
  assertEquals(monadicParsing("21", "m", "r"),
               Result(NumberFormatException("For input string: \"m\"")))
}

function test_catcher = {
  let recover = catcher(|ex| -> match {
    when ex oftype IllegalArgumentException.class then "default"
    otherwise ex: toString()
  })

  assertEquals(recover({ return "answer" }), "answer")
  assertEquals(recover({ throw IllegalArgumentException() }), "default")
  assertEquals(recover({ raise("err") }), "java.lang.RuntimeException: err")
}

@!catcher
function recoverIAE = |ex| -> match {
  when ex oftype IllegalArgumentException.class then "default"
  otherwise ex: toString()
}

function test_catcher_decorator = {
  assertEquals(recoverIAE({ return "answer" }), "answer")
  assertEquals(recoverIAE({ throw IllegalArgumentException() }), "default")
  assertEquals(recoverIAE({ raise("err") }), "java.lang.RuntimeException: err")
}

# ........................................................................... #
function main = |args| {
  test_and()
  test_or()
  test_reduce()
  test_catching()
  test_result()
  test_closure_map()
  test_flatMap()
  test_andThen()
  test_trying()
  test_raising()
  test_orElseGet()
  test_applicative()
  test_imperative()
  test_monadic()
  test_catcher()
  test_catcher_decorator()
  println("ok")
}
