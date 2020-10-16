
module golo.test.MacroUtils

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

import gololang.macros.Utils
import gololang.ir
import gololang.ir.DSL

function assertRaises = |f, t| {
  try {
    f()
    raise("must fail")
  } catch (e) {
    assertThat(e, isA(t))
  }
}

# ........................................................................... #

function test_extractLastArgument = {
  var b = null
  var e = null

  b, e = extractLastArgument(array[1, 2, 3, 4])
  assertThat(b, `is(arrayContaining(1, 2, 3)))
  assertThat(e, `is(4))

  b, e = extractLastArgument(array["foo"])
  assertThat(b, `is(emptyArray()))
  assertThat(e, `is("foo"))

  assertRaises(
    -> extractLastArgument(array[]),
    NegativeArraySizeException.class)
}

function test_namedArgsToMap = {
  var r = null

  r = namedArgsToMap(array[
    namedArgument("answer", 42),
    namedArgument("gname", "Ford")
  ]): map(|k, v| -> mapEntry(k, v: value()))

  assertThat(r, `is(map[
    ["answer", 42],
    ["gname", "Ford"]
  ]))

  r = namedArgsToMap(array[])
  assertThat(r, `is(map[]))

  r = namedArgsToMap(array[
    namedArgument("gname", "Zaphod"),
    constant("answer"),
    namedArgument("name", "Beeblebrox"),
    42
  ]): map(|k, v| -> mapEntry(k, v: value()))
  assertThat(r, `is(map[
    ["gname", "Zaphod"],
    ["name", "Beeblebrox"]
  ]))

}

function test_parseArguments = {
  assertThat(
    parseArguments(array[], false),
  arrayContaining(
    array[], map[], null
  ))

  assertRaises(
    -> parseArguments(array[], true),
    NegativeArraySizeException.class)

  assertThat(
    parseArguments(array[42], false),
  arrayContaining(
    array[42], map[], null
  ))

  assertThat(
    parseArguments(array[42]),
  arrayContaining(
    array[42], map[], null
  ))

  assertThat(
    parseArguments(array[42], true),
  arrayContaining(
    array[], map[], 42
  ))

  assertThat(
    parseArguments(array[42], null),
  arrayContaining(
    array[], map[], 42
  ))

  assertThat(
    parseArguments(array[42], Integer.class),
  arrayContaining(
    array[], map[], 42
  ))

  assertThat(
    parseArguments(array[42], String.class),
  arrayContaining(
    array[42], map[], null
  ))

  let args = array[
    namedArgument("gname", "Zaphod"),
    constant("answer"),
    namedArgument("name", "Beeblebrox"),
    constant(42),
    constant("last")
  ]
  var l, m, r = [null, null, null]


  l, m, r = parseArguments(args, false)
  assertThat(
    array[c: value() foreach c in l],
  `is(
    array["answer", 42, "last"]
  ))

  assertThat(
    m: map(|k, v| -> mapEntry(k, v: value())),
  `is(
    map[
      ["gname", "Zaphod"],
      ["name", "Beeblebrox"]
    ]
  ))

  l, m, r = parseArguments(args, true)
  assertThat(
    array[c: value() foreach c in l],
  `is(
    array["answer", 42]
  ))

  assertThat(
    m: map(|k, v| -> mapEntry(k, v: value())),
  `is(
    map[
      ["gname", "Zaphod"],
      ["name", "Beeblebrox"]
    ]
  ))

  assertThat(r: value(), `is("last"))
}

augment gololang.ir.GoloModule {
  function name = |this| -> this: packageAndClass(): toString()
}

function test_toplevel = {
  var r = null
  let replace = |e| -> `function("replaced_" + e: name())
  let check = |n, r| {
    assertThat(r, isA(GoloFunction.class))
    assertThat(r: name(), `is("replaced_" + n))
  }

  # on module
  check("Test", toplevel(GoloModule.class)(replace)(`module("Test")))

  # on single valid type
  check("foo", toplevel(GoloFunction.class)(replace)(`function("foo")))

  # on single bad type
  let s = `struct("Foo"): members("x", "y")
  assertThat(toplevel(GoloFunction.class)(replace)(s), `is(s))

  # on mixed varargs
  let t = `struct("Bar")
  let a = `augmentation("xxx")
  r = toplevel(GoloFunction.class)(replace)(
    s,
    `function("foo"),
    t,
    `function("plop"),
    a
  )
  assertThat(r, isA(ToplevelElements.class))
  var l = list[e foreach e in r]
  assertThat(l: size(), `is(5))
  assertThat(l: get(0), `is(s))
  check("foo", l: get(1))
  assertThat(l: get(2), `is(t))
  check("plop", l: get(3))
  assertThat(l: get(4), `is(a))

  # on mixed toplevel
  r = toplevel(GoloFunction.class)(replace)(toplevels(
    s,
    `function("foo"),
    t,
    `function("plop"),
    a
  ))
  assertThat(r, isA(ToplevelElements.class))
  l = list[e foreach e in r]
  assertThat(l: size(), `is(5))
  assertThat(l: get(0), `is(s))
  check("foo", l: get(1))
  assertThat(l: get(2), `is(t))
  check("plop", l: get(3))
  assertThat(l: get(4), `is(a))
}

function test_thisModule = {
  assertThat(&thisModule("plop"), `is("golo.test.MacroUtils.plop"))
}

function main = |args| {
  test_parseArguments()
  # test_extractLastArgument()
  # test_namedArgsToMap()
  # test_toplevel()
  # test_thisModule()
}
