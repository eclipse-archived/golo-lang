
module golotest.augmentations.TestComplexDispatch

import golotest.augmentations.MyLib
import golotest.augmentations.MyData.types
import golotest.augmentations.MyData
import golotest.augmentations.MixinAugmentation

augment java.lang.String with golotest.augmentations.MixinAugmentation.Plopable
augment java.lang.String {
  function bar = |this| -> "bar:str:" + this
}

augment java.util.Set {
  function isPlopable = |this| -> true
  function plop = |this| -> "set:plop" + this: toString()
}

augment java.util.List with Plopable
augment java.util.List {
  function plop = |this| -> "list:plop" + this: toString()
}

local function assertEquals = |f, v| {
  require(f == v, v + " expected, got " + f)
}

function test_dispatch = {
  assertEquals(sayPlop("42"), "say:plop:java.lang.String:bar:str:42")
  assertEquals(sayPlop(list[42]), "say:list:plop[42]")
  assertEquals(sayPlop(set[42]), "say:set:plop[42]")

  assertEquals(sayPlop(42), "no plop:42")

  assertEquals(sayPlop(MyStruct(42)),
    "say:plop:golotest.augmentations.MyData.types.MyStruct:42")

  assertEquals(sayPlop(MyUnion.Not()),
    "no plop:union MyUnion.Not")
  assertEquals(sayPlop(MyUnion.Yes(42)),
    "say:plop:golotest.augmentations.MyData.types.MyUnion$Yes:bar:Yes:42")
}

function main = |args| {
  test_dispatch()
  println("ok")
}
