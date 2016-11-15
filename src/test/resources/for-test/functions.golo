module golo.test.Functions

import org.hamcrest.MatcherAssert
import org.hamcrest

import gololang.Functions

local function assertEquals = |value, expected| {
  assertThat(value, Matchers.equalTo(expected))
}

# ........................................................................... #
struct Plop = { a, b, c }

union Tada = {
  C = { a, b, c }
}

function foo = |a, b, cs...| {
  let l = list[a, b]
  l: addAll(cs: asList())
  return l: join(":")
}

function varonly = |args...| -> args: asList(): join(":")

function bar = |a, b, c| -> a + b + c

function curried = |a| -> |b| -> |c| -> a + b + c

function curriedvar = |a| -> |b| -> |c...| ->
  a + ":" + b + ":" + c: asList(): join(":")

# ........................................................................... #
function test_add = {
  assertEquals(add(3, 4), 7)
  assertEquals(add("b", "a"), "ab")

  assertEquals(list[1, 2, 3]: map(add(3)), list[4, 5, 6])

  assertEquals(add(2.5, 4.2), 6.7)

  let f = add(42): andThen(add(1337)): andThen(add(69))
  assertEquals(f(list[]), list[42, 1337, 69])
  assertEquals(f(0), 1448)
  assertEquals(f(StringBuilder()): toString(), "42133769")

  assertEquals(add("a", StringBuilder()): toString(), "a")
}

function test_operators = {
  assertEquals(succ(41), 42)
  assertEquals(succ(1.2), 2.2)
  assertEquals(pred(43), 42)
  assertThat(pred(2.2), Matchers.closeTo(1.2, 0.0001))

  assertEquals(mul(2, 21), 42)
  assertEquals(mul(2)(21), 42)

  assertEquals(neg(5), -5)
  assertEquals(neg(5_L), -5_L)
  assertEquals(neg(2.5), -2.5)

  assertEquals(sub(3, 2), 1)
  assertEquals(sub(2, 3), -1)
  assertEquals(sub(2.5, 0.5), 2.0)
  assertEquals(sub(2)(1), 1)
  assertEquals(rsub(2, 3), 1)
  assertEquals(rsub(1)(2), 1)

  assertThat(div(1, 2), Matchers.equalTo(0))
  assertThat(div(1.0, 2.0), Matchers.closeTo(0.5, 0.001))
  assertThat(div(1)(2.0), Matchers.closeTo(0.5, 0.001))
  assertThat(div(1.0)(2), Matchers.closeTo(0.5, 0.001))

  assertThat(rdiv(2, 1), Matchers.equalTo(0))
  assertThat(rdiv(2.0, 1.0), Matchers.closeTo(0.5, 0.001))
  assertThat(rdiv(2)(1.0), Matchers.closeTo(0.5, 0.001))
  assertThat(rdiv(2.0)(1), Matchers.closeTo(0.5, 0.001))

  assertEquals(`orIfNull(0, 3), 3)
  assertEquals(`orIfNull(0, null), 0)
  assertEquals(`orIfNull(0)(3), 3)
  assertEquals(`orIfNull(0)(null), 0)

  assertEquals(`is(null)(null), true)
  assertEquals(`is(null)(42), false)
  assertEquals(`isnt(null)(42), true)
  assertEquals(`isnt(null)(null), false)

  assertEquals(`oftype(Integer.class)(42), true)
  assertEquals(`oftype(Integer.class)("foo"), false)

  let predicate = contains(42)
  assertEquals(predicate(list[1, 42, 3]), true)
  assertEquals(predicate(list[1, 3]), false)
}

function test_booleans = {
  assertEquals(`and(true, true), true)
  assertEquals(`and(false, true), false)
  assertEquals(`and(false)(true), false)
  assertEquals(`and(true)(true), true)

  assertEquals(`and(^gololang.Functions::id, ^gololang.Functions::id)(true), true)
  assertEquals(`and(^gololang.Functions::id)(^gololang.Functions::id)(true), true)
  assertEquals(`and(^gololang.Functions::id)(^gololang.Functions::id, true), true)
  assertEquals(`and(^gololang.Functions::id, ^gololang.Functions::id, true), true)

  assertEquals(`and(^gololang.Functions::id, ^gololang.Functions::id)(false), false)
  assertEquals(`and(^gololang.Functions::id, ^gololang.Functions::not)(true), false)
  assertEquals(`and(^gololang.Functions::id, ^gololang.Functions::not)(false), false)

  assertEquals(`or(true, true), true)
  assertEquals(`or(false, true), true)
  assertEquals(`or(false, false), false)
  assertEquals(`or(false)(true), true)
  assertEquals(`or(true)(true), true)
  assertEquals(`or(false)(false), false)
  assertEquals(`or(^gololang.Functions::id, ^gololang.Functions::id)(true), true)
  assertEquals(`or(^gololang.Functions::id)(^gololang.Functions::id)(true), true)
  assertEquals(`or(^gololang.Functions::id)(^gololang.Functions::id, true), true)
  assertEquals(`or(^gololang.Functions::id, ^gololang.Functions::id, true), true)

  assertEquals(`or(^gololang.Functions::id, ^gololang.Functions::id)(false), false)
  assertEquals(`or(^gololang.Functions::id, ^gololang.Functions::not)(true), true)
  assertEquals(`or(^gololang.Functions::id, ^gololang.Functions::not)(false), true)

  assertEquals(xor(true, true), false)
  assertEquals(xor(true, false), true)
  assertEquals(xor(false, true), true)
  assertEquals(xor(false, false), false)
  assertEquals(xor(true)(true), false)
  assertEquals(xor(true)(false), true)
  assertEquals(xor(false)(true), true)
  assertEquals(xor(false)(false), false)
}

function test_extractors = {
  assertEquals(fst([1, 2]), 1)
  assertEquals(snd([1, 2]), 2)
  assertEquals(getitem([1, 2, 3], 2), 3)
  assertEquals(getter(2)([1, 2, 3]), 3)
}

function test_not = {
  assertEquals(`not(true), false)
  assertEquals(`not(false), true)
  assertEquals(`not(^gololang.Functions::isEmpty)(list[1]), true)

  assertEquals(list[true, false, false]: map(^gololang.Functions::not),
               list[false, true, true])

  let e = |a| -> (a % 2) == 0
  let o = `not(e)

  assertEquals(o(3), true)
  assertEquals(o(2), false)
}

function test_const = {
    let theAnswerTo = const(42)

    assertEquals(theAnswerTo(), 42)
    assertEquals(theAnswerTo(1, 2, 3), 42)
    assertEquals(theAnswerTo("Life, the Universe and Everything"), 42)
}

function test_until = {
  let f = until(gt(10), mul(2))

  assertEquals(f(15), 15)
  assertEquals(f(2), 16)
  assertEquals(f(9), 18)
}

function test_curry = {
  let barc = curry(^bar)
  assertEquals(42, barc(29, 10, 3))
  assertEquals(42, barc(29)(10, 3))
  assertEquals(42, barc(29)(10)(3))
  assertEquals(42, barc(29, 10)(3))

  let barc2 = barc(29)
  let barc3 = barc2(10)
  let barc4 = barc(29)(10)
  assertEquals(42, barc2(10)(3))
  assertEquals(42, barc3(3))
  assertEquals(42, barc4(3))

  let f = barc(2, 20): andThen(barc("The answer", " is "))
  assertEquals(f(20), "The answer is 42")
}

function test_base_varargs = {
  assertEquals(foo("a", "b"), "a:b")
  assertEquals(foo("a", "b", "c"), "a:b:c")
  assertEquals(foo("a", "b", "c1", "c2"), "a:b:c1:c2")
  assertEquals(foo("a", "b", array["c1", "c2"]), "a:b:c1:c2")

  assertEquals(varonly("a", "b", "c"), "a:b:c")
}

function test_curry_varargs = {
  let fooc = curry(^foo)
  assertEquals(fooc("a", "b", "c"), "a:b:c")
  assertEquals(fooc("a", "b", "c1", "c2"), "a:b:c1:c2")
  assertEquals(fooc("a", "b")(), "a:b")
  assertEquals(fooc("a", "b")("c"), "a:b:c")
  assertEquals(fooc("a", "b")("c1", "c2"), "a:b:c1:c2")
  assertEquals(fooc("a", "b")(array["c1", "c2"]), "a:b:c1:c2")
  assertEquals(fooc("a")("b")("c"), "a:b:c")
  assertEquals(fooc("a")("b")("c1", "c2"), "a:b:c1:c2")
  assertEquals(fooc("a")("b")(array["c1", "c2"]), "a:b:c1:c2")
  assertEquals(fooc("a")("b", "c"), "a:b:c")
  assertEquals(fooc("a")("b", "c1", "c2"), "a:b:c1:c2")
}

function test_arity = {
  assertEquals((-> "dhoo"): arity(), 0)
  assertEquals((|a| -> "dhoo"): arity(), 1)
  assertEquals((|a...| -> "dhoo"): arity(), 1)
  assertEquals((|a, b| -> "dhoo"): arity(), 2)
  assertEquals((|a, b...| -> "dhoo"): arity(), 2)
}

function test_iob = {
  assertEquals(^bar: invokeOrBind("a"): invokeOrBind("b"): invokeOrBind("c"),
               "abc")
  assertEquals(^bar: invokeOrBind("a"): invoke("b", "c"), "abc")
  assertEquals(^bar: invokeOrBind("a")("b", "c"), "abc")
  assertEquals(^bar: invokeOrBind("a", "b")("c"), "abc")
  assertEquals(^bar: invokeOrBind("a", "b", "c"), "abc")
}

function test_iob_varargs = {
  assertEquals(^foo: invokeOrBind("a", "b", "c"), "a:b:c")
  assertEquals(^foo: invokeOrBind("a", "b", "c1", "c2"), "a:b:c1:c2")
  assertEquals(^foo: invokeOrBind("a", "b"): invoke(array["c1", "c2"]),
                "a:b:c1:c2")

  assertEquals(^foo: invokeOrBind("a", "b")(), "a:b")
  assertEquals(^foo: invokeOrBind("a", "b")("c"), "a:b:c")
  assertEquals(^foo: invokeOrBind("a", "b")("c1", "c2"), "a:b:c1:c2")

  assertEquals(^foo: invokeOrBind("a")("b", "c"), "a:b:c")
  assertEquals(^foo: invokeOrBind("a")("b", "c1", "c2"), "a:b:c1:c2")
}

function test_unary = {
  let bu = unary(^bar)
  assertEquals(bu(array["a", "b", "c"]), "abc")
  assertEquals(bu(tuple["a", "b", "c"]), "abc")
  assertEquals(bu(list["a", "b", "c"]), "abc")
  assertEquals(bu(Plop("a", "b", "c")), "abc")
  assertEquals(bu(C("a", "b", "c")), "abc")
}

function test_unary_varargs = {
  let fu = unary(^foo)
  assertEquals(fu(array["a", "b"]), "a:b")
  assertEquals(fu(array["a", "b", "c"]), "a:b:c")
  assertEquals(fu(tuple["a", "b", "c"]), "a:b:c")
  assertEquals(fu(list["a", "b", "c"]), "a:b:c")
  assertEquals(fu(Plop("a", "b", "c")), "a:b:c")
  assertEquals(fu(C("a", "b", "c")), "a:b:c")
  assertEquals(fu(array["a", "b", "c", "d"]), "a:b:c:d")

  let vu = unary(^varonly)
  assertEquals(vu(array["a", "b", "c"]), "a:b:c")
  assertEquals(vu(tuple["a", "b", "c"]), "a:b:c")
  assertEquals(vu(list["a", "b", "c"]), "a:b:c")
  assertEquals(vu(Plop("a", "b", "c")), "a:b:c")
  assertEquals(vu(C("a", "b", "c")), "a:b:c")
}

function test_varargs = {
  let f = varargs(|a| -> a: get(0) + a: get(1) + a: get(2))
  let g = varargs(|a| -> a: asList(): join(":"))

  assertEquals(f("a", "b", "c"), "abc")
  assertEquals(g("a", "b", "c"), "a:b:c")
}

function test_spreader = {
  let bu = spreader(^bar)
  assertEquals(bu(array["a", "b", "c"]), "abc")
  assertEquals(bu(tuple["a", "b", "c"]), "abc")
  assertEquals(bu(list["a", "b", "c"]), "abc")
  assertEquals(bu(Plop("a", "b", "c")), "abc")
  assertEquals(bu(C("a", "b", "c")), "abc")
}

function test_spreader_varargs = {
  let fu = spreader(^foo)
  assertEquals(fu(array["a", "b", array[]]), "a:b")
  assertEquals(fu(array["a", "b", "c"]), "a:b:c")
  assertEquals(fu(tuple["a", "b", "c"]), "a:b:c")
  assertEquals(fu(list["a", "b", "c"]), "a:b:c")
  assertEquals(fu(Plop("a", "b", "c")), "a:b:c")
  assertEquals(fu(C("a", "b", "c")), "a:b:c")
  assertEquals(fu(array["a", "b", array["c", "d"]]), "a:b:c:d")
}

function test_uncurry = {
  let uc = uncurry(^curried)
  assertEquals("abc", uc()()()("a", "b", "c"))
  assertEquals("abc", uc("a", "b", "c"))
  assertEquals("abc", uc(array["a", "b", "c"]))
  assertEquals("abc", uc("a")("b", "c"))
  assertEquals("abc", uc("a")("b")("c"))
  assertEquals("abc", uc("a", "b")("c"))
}

function test_uncurry_varargs = {
  let uc = uncurry(^curriedvar)
  assertEquals(uc("a", "b", "c"), "a:b:c")
  assertEquals(uc("a", "b", "c1", "c2"), "a:b:c1:c2")
  assertEquals(uc("a", "b")(), "a:b:")
  assertEquals(uc("a", "b")("c"), "a:b:c")
  assertEquals(uc("a", "b")("c1", "c2"), "a:b:c1:c2")
  assertEquals(uc("a", "b")(array["c1", "c2"]), "a:b:c1:c2")
  assertEquals(uc("a")("b")("c"), "a:b:c")

  assertEquals(uc("a")("b")("c1", "c2"), "a:b:c1:c2")
  assertEquals(uc("a")("b")(array["c1", "c2"]), "a:b:c1:c2")
  assertEquals(uc("a")("b", "c"), "a:b:c")
  assertEquals(uc("a")("b", "c1", "c2"), "a:b:c1:c2")
}

function test_swap = {
  assertEquals(swap(tuple[1, 2]), tuple[2, 1])
  assertEquals(swap(list[1, 2]), list[2, 1])
  assertEquals(swap(array[1, 2]), array[2, 1])
  assertEquals(swap(vector[1, 2]), vector[2, 1])

  let c = |a| -> |b| -> a + b
  let f = |a, b| -> a + b

  assertEquals(swap(c)("a")("b"), "ba")
  assertEquals(swap(f)("a", "b"), "ba")

  assertEquals(unary(f)(swap(["a", "b"])), unary(swap(f))(["a", "b"]))
}

function test_swap_varargs = {
  let v = |a, b...| -> a + ":" + b: asList(): join(":")
  let vv = |a...| -> a: get(0) + a: get(1)
  assertEquals(swap(vv)("a", "b"), "ba")
  assertEquals(swap(vv)(array["a", "b"]), "ba")
  assertEquals(swap(v)(array["a", "b"], "c"), "c:a:b")
}

function test_swap_curry_uncurry = {
  let c = uncurry(|a| -> |b| -> a + b)
  let f = curry(|a, b| -> a + b)
  assertEquals(swap(c)("a", "b"), "ba")
  assertEquals(swap(f)("a", "b"), "ba")
}

function test_invokeWith = {
  let f = |a| -> "f: " + a

  assertEquals(invokeWith(42)(f), "f: 42")
  assertEquals(invokeWith("a", "b", "c")(^bar), "abc")
  assertEquals(invokeWith(array["a", "b", "c"])(^bar), "abc")
  assertEquals(invokeWith("a", "b", "c", "d")(^foo), "a:b:c:d")
  assertEquals(invokeWith("a", "b", "c")(^varonly), "a:b:c")
  assertEquals(invokeWith(array["a", "b", "c"])(^varonly), "a:b:c")
}

function test_io = {
  let l = list[]

  let i1 = io({l: add(1)})
  let i2 = io(|x| {l: add(x)})
  let i3 = io(|x| {
    l: add(x)
    return x * 2
  })

  let p = ^gololang.Functions::id
          : andThen(^gololang.Functions::succ)
          : andThen(i1)
          : andThen(i2)
          : andThen(i3)
          : andThen(^gololang.Functions::succ)

  let r = p(1)
  assertEquals(r, 5)
  assertEquals(l, list[1, 2, 2])
}

function test_pipe = {
  let p = pipe(add(1), mul(2), add("2"))
  assertEquals(p(1), "42")

  let l = list[add(1), mul(2), add("2")]
  assertEquals(pipe(l: toArray())(1), "42")

  assertEquals(pipe()(42), 42)

  let cmd1 = pipe(mul(2), add(4), ^gololang.Functions::str, addTo("val: "))
  assertEquals(cmd1(19), "val: 42")

  let cmd2 = pipe(^gololang.Predefined::intValue, add(4), mul(2))
  assertEquals(cmd2("17"), 42)
}

function test_compose = {
  let p = compose(add("2"), mul(2), add(1))
  assertEquals(p(1), "42")

  let l = list[add("2"), mul(2), add(1)]
  assertEquals(compose(l: toArray())(1), "42")

  assertEquals(compose()(42), 42)
}

function main = |args| {
}
