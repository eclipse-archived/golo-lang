
module golo.test.TopLevelTest

import org.testng.Assert

&use("golo.test.TopLevelMacros")

&createFunction(foo, 42)

&createStruct(Foo, a, b, c)

augment Foo {
  &augmentationFunction(bar, 1337)
  &contextualAugmentationFunction(answer, 42)
}

augmentation DoPloc = {
  &augmentationFunction(ploc, "plic")
  &metaAugmentationFunction()
}

&defState(const, 1234)

augment java.lang.String with DoPloc

function test = {
  assertEquals(foo(), 42)
  assertEquals(Foo(1, 2, 3): bar(), 1337)
  assertEquals("": ploc(), "plic")
  assertEquals("": meta(), 666)
  assertEquals(const, 1234)
}

function main = |args| {
  test()
}
