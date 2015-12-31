
module golotest.execution.NamedAugmentations

import golotest.execution.NamedAugmentations.ExternalSource

struct MyStruct = {a}

augment MyStruct with golotest.execution.NamedAugmentations.ExternalSource.Bar1

augment java.lang.Double with Bar2

augment MyStruct {
  function spam = |this| -> "MyStruct.spam"
}

local function assertEquals = |f, v| {
  require(f == v, v + " expected, got " + f)
}

function test_foo_on_string = {
  assertEquals("": foo(), "Str.foo")
}

function test_bar_on_string = {
  assertEquals("": bar(), "Bar1.bar")
}

function test_foo_on_int = {
  assertEquals(1: foo(), "Foo.foo")
}

function test_bar_on_int = {
  assertEquals(1: bar(), "Bar2.bar")
}

function test_spam_on_int = {
  assertEquals(1: spam(), "Spam.spam")
}

function test_foo_on_struct = {
  assertEquals(MyStruct(1): foo(), "Obj.foo")
}

function test_bar_on_struct = {
  assertEquals(MyStruct(1): bar(), "Bar1.bar")
}

function test_bar_on_double = {
  assertEquals(1.0: bar(), "Bar2.bar")
}

function test_override_spam_on_struct = {
  assertEquals(MyStruct(1): spam(), "MyStruct.spam")
}

function main = |args| {
  test_foo_on_string()
  test_bar_on_string()
  test_foo_on_int()
  test_bar_on_int()
  test_spam_on_int()
  test_foo_on_struct()
  test_bar_on_struct()
  test_bar_on_double()
  test_override_spam_on_struct()
  println("ok")
}
