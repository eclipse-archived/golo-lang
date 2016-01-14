module golotest.execution.LocalNamedAugmentations

import java.util.LinkedList

augmentation OnList = {
  function plop = |this| -> "plop"
}

augmentation FooBar = {
  function foo = |this| -> "foo"
  function bar = |this| -> "bar"
}

augment java.util.List {
  function baz = |this| -> "baz"
}

augment java.util.List with OnList, FooBar

struct MyStruct = {a, b}
augment MyStruct with FooBar

#== tests ===

function test_plop_on_list = {
  require(list[]: plop() == "plop", "err plop on list")
}

function test_foo_on_list = {
  require(list[]: foo() == "foo", "err foo on list")
}

function test_bar_on_list = {
  require(list[]: bar() == "bar", "err bar on list")
}

function test_baz_on_list = {
  require(list[]: baz() == "baz", "err baz on list")
}

function test_foo_on_struct = {
  require(MyStruct(1, 2): foo() == "foo", "err foo on struct")
}

function test_bar_on_struct = {
  require(MyStruct(1, 2): bar() == "bar", "err bar on struct")
}

function main = |args| {
  test_plop_on_list()
  test_foo_on_list()
  test_bar_on_list()
  test_baz_on_list()
  test_foo_on_struct()
  test_bar_on_struct()
  println("ok")
}
