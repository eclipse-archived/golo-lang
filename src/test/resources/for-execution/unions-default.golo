module golotest.execution.UnionsDefaults

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.testng.Assert

union MyUnion = {
  Type1
  Type2 = {a, b=2*a}
  Type3 = {y, z=42}
  Type4 = {v=MyUnion.Type1()}
  Type5 = {x, y=foo()}
  Type6 = {a, b, c=a+b}
}

let state = list[]

function foo = {
  state: add("foo")
  return 42
}

function test_constant_default_union = {
  let full = Type3("a", 1337)
  assertThat(full: y(), `is("a"))
  assertThat(full: z(), `is(1337))

  let def = Type3("a")
  assertThat(def: y(), `is("a"))
  assertThat(def: z(), `is(42))
}

function test_computed_default_union = {
  assertThat(state: size(), `is(0))

  let full = Type5("a", 1337)
  assertThat(state: size(), `is(0))
  assertThat(full: x(), `is("a"))
  assertThat(full: y(), `is(1337))

  let def = Type5("a")
  assertThat(state: size(), `is(1))
  assertThat(def: x(), `is("a"))
  assertThat(def: y(), `is(42))
}

function test_dependant_default_union = {
  let full = Type2(1, 42)
  assertThat(full: a(), `is(1))
  assertThat(full: b(), `is(42))

  let def = Type2(21)
  assertThat(def: a(), `is(21))
  assertThat(def: b(), `is(42))
}

function test_recursive_default_union = {
  let full = Type4(42)
  assertThat(full: v(), `is(42))

  let def = Type4()
  assertTrue(def: v(): isType1())
}

function test_multiple_dependant_union = {
  let full = Type6(1, 2, 42)
  assertThat(full: a(), `is(1))
  assertThat(full: b(), `is(2))
  assertThat(full: c(), `is(42))

  let def = Type6(25, 17)
  assertThat(def: a(), `is(25))
  assertThat(def: b(), `is(17))
  assertThat(def: c(), `is(42))
}

union Foo = {
  Bar = { value }
  Baz = {a, b=Bar(a)}
}

function test_self_dependant_value = {
  assertThat(Baz(42): toString(),
    `is("union Foo.Baz{a=42, b=union Foo.Bar{value=42}}"))
}
