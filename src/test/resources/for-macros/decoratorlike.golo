@moduleMacro
module golo.test.decoratorTest

import org.testng.Assert

&use("golo.test.decoratorMacros")

# ............................................................................ #
# On functions

function regular = |f| -> |args...| {
  return "regular " + f: invoke(args)
}

@regular
function f0 = -> "f0"

&deco {
function f1 = -> "f1"
}

&deco {
@regular
function f2 = -> "f2"
}

&deco2("tada") {
function f3 = -> "f3"
}

@deco
function f4 = -> "f4"

@deco
@regular
function f5 = -> "f5"

@regular
@deco
function f6 = -> "f6"

@deco2("tada")
function f7 = -> "f7"

@deco
@deco
function f8 = -> "f8"

@deco2("tada")
@deco
function f9 = -> "f9"

@deco3
function f10 = -> "f10"

@deco3
function f10 = |a, b| -> "f10:" + a + ',' + b

@deco3
function f10v = |a...| -> "f10v:" + a: asList(): join(",")

&deco3 {
function f11 = -> "f11"
}

@deco2("Youhou")
@deco3
function f12 = -> "f12"

@decoMod("injected")
function f13 = |a, b| -> "f13:" + a + ',' + b


&decoMod("injectedm") {
function f14 = |a, b| -> "f14:" + a + ',' + b
}

function test_functions = {
  assertEquals(f0(), "regular f0")
  assertEquals(f1(), "deco f1")
  assertEquals(f2(), "regular deco f2")
  assertEquals(f3(), "deco2 tada f3")
  assertEquals(f4(), "deco f4")
  assertEquals(f5(), "regular deco f5")
  assertEquals(f6(), "regular deco f6")
  assertEquals(f7(), "deco2 tada f7")
  assertEquals(f8(), "deco deco f8")
  assertEquals(f9(), "deco2 tada deco f9")
  assertEquals(f10(), "deco3 f10")
  assertEquals(f10(b='b', a='a'), "deco3 f10:a,b")
  assertEquals(f10v('a', 'b', 'c', 'd', 'e', 'f'),
    "deco3 f10v:a,b,c,d,e,f")
  assertEquals(f11(), "deco3 f11")
  assertEquals(f12(), "deco2 Youhou deco3 f12")
  assertEquals(injectedf13(b="b", a="a"), "injected f13:a,b")
  assertEquals(injectedmf14(b="b", a="a"), "injectedm f14:a,b")
}

@decoOverloaded
function foverdefault = |a| -> "over:" + a

@decoOverloaded("message")
function fover = |a| -> "over:" + a

function test_overloaded_macro = {
  assertEquals(foverdefault(42), "default over:42")
  assertEquals(fover(42), "message over:42")
}

# ............................................................................ #
# On structs

@decostruct
struct Foo = { get }

@decostructmember("args")
@decostructfun
struct Bar = { get }

@decostructfun
@decostructmember("args")
struct Baz = { get }

function test_struct = {
  assertEquals(newFoo(b="2", a="4"): get(), "42")
  assertEquals(newFoo(b="2", a="4"): args(), ["2", "4"])
  assertEquals(newBar(b="2", a="4"): get(), "42")
  assertEquals(newBar(b="2", a="4"): args(), ["2", "4"])
  assertEquals(newBaz(b="2", a="4"): get(), "42")
  assertEquals(newBaz(b="2", a="4"): args(), ["2", "4"])
}

# ............................................................................ #
# On named augmentations

@withTest
augmentation Plopable = {
  function plop = |this| -> "plop"
}

@withConstant("baz")
@withTest
@withConstant("foo")
augmentation Barable = {
  function bar = |this| -> "bar"
}

augment java.lang.String with Plopable

augment java.lang.Integer with Barable

function test_augmentation = {
  assertEquals("": plop(), "plop")
  assertEquals("": isPlopable(), true)
  assertEquals(42: isPlopable(), false)

  assertEquals(42: foo(), "foo")
  assertEquals(42: bar(), "bar")
  assertEquals(42: baz(), "baz")
  assertEquals(42: isBarable(), true)
  assertEquals("": isBarable(), false)
}

# ............................................................................ #
# On augments and functions inside augments

augment java.lang.String {
  @deco
  function foobar = |this| -> "foobar " + this
}

function test_on_augment_fun = {
  assertEquals("aa": foobar(), "deco foobar aa")
}

augmentation MacroOnAugmentationFun = {
  @deco
  function barfoo = |this| -> "barfoo " + this
}

augment java.lang.String with MacroOnAugmentationFun

function test_on_augmentation_fun = {
  assertEquals("aa": barfoo(), "deco barfoo aa")
}

@withConstant("abcde")
augment java.lang.String {
  function dummy = |this| -> "dummy " + this
}

function test_on_augment = {
  assertEquals("a": dummy(), "dummy a")
  assertEquals("a": abcde(), "abcde")
}

# ............................................................................ #
# On unions and union values

@withConstant("plop")
@decounion
union UnionTest = {
  Empty
  One = {x}
  Two = {a, b}
}

function test_union = {
  assertEquals(UnionTest(), UnionTest.Empty())
  assertEquals(UnionTest(1), UnionTest.One(1))
  assertEquals(UnionTest(1, 2), UnionTest.Two(1, 2))
  assertEquals(UnionTest.Empty(): plop(), "plop")
  assertEquals(UnionTest.One(1): plop(), "plop")

  try {
    UnionTest(1, 2, 3)
    raise("must fail")
  } catch (e) {
    assertEquals(e: message(), "invalid number or arguments")
  }
}

union UnionValueTest = {
  @withConstant("plop")
  Empty
  @withConstant("daplop")
  Other = {x}
}

function test_union_value = {
  let e = UnionValueTest.Empty()
  let o = UnionValueTest.Other(1)
  assertEquals(e: plop(), "plop")
  assertEquals(o: daplop(), "daplop")

  try {
    o: plop()
    raise("must fail")
  } catch (err) {
    assertTrue(err oftype java.lang.NoSuchMethodError.class)
  }
  try {
    e: daplop()
    raise("must fail")
  } catch (err) {
    assertTrue(err oftype java.lang.NoSuchMethodError.class)
  }
}

function test_module = {
  assertEquals(generatedByModuleMacro(), "module macro result")
}

function main = |args| {
  test_functions()
  test_overloaded_macro()
  test_struct()
  test_augmentation()
  test_on_augment_fun()
  test_on_augmentation_fun()
  test_on_augment()
  test_union()
  test_union_value()
  test_module()
}
