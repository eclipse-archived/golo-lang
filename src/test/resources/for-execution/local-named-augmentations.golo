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
  return list[]: plop() # should be plop
}

function test_foo_on_list = {
  return list[]: foo() # should be foo
}

function test_bar_on_list = {
  return list[]: bar() # should be bar
}

function test_baz_on_list = {
  return list[]: baz() # should be baz
}

function test_foo_on_struct = {
  return MyStruct(1, 2): foo() # should be foo
}

function test_bar_on_struct = {
  return MyStruct(1, 2): bar() # should be bar
}

function main = |args| {
  require(test_plop_on_list() == "plop", "err")
  require(test_foo_on_list() == "foo" , "err")
  require(test_bar_on_list() == "bar" , "err")
  require(test_baz_on_list() == "baz" , "err")
  require(test_foo_on_struct() == "foo" , "err")
  require(test_bar_on_struct() == "bar" , "err")
}
