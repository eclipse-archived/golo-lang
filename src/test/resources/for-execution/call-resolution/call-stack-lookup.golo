module golotest.augmentationScope.CallStackLookup

import golotest.augmentationScope.MyData
import golotest.augmentationScope.MyLib

augment java.lang.String {
  function size = |this| -> this: length()
}

function test_on_string = {
  require(getSize("hello") == 5, "err on string")
}

function test_on_struct = {
  require(getSize(MyStruct(42)) == 42, "err on struct")
}

function test_named_augmentation = {
  require(not MyStruct(42): isEmpty(), "err")
  require(MyStruct(0): isEmpty(), "err")
}

function main = |args| {
  test_on_string()
  test_on_struct()
  test_named_augmentation()
  println("ok")
}
