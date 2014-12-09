
module golotest.execution.NamedAugmentations

import golotest.execution.NamedAugmentations.ExternalSource

struct MyStruct = {a}

augment MyStruct with golotest.execution.NamedAugmentations.ExternalSource.Bar1

augment java.lang.Double with Bar2

augment MyStruct {
  function spam = |this| -> "MyStruct.spam"
}

function foo_on_string = -> "": foo()

function bar_on_string = -> "": bar()

function foo_on_int = -> 1: foo()

function bar_on_int = -> 1: bar()

function spam_on_int = -> 1: spam()

function foo_on_struct = -> MyStruct(1): foo()

function bar_on_struct = -> MyStruct(1): bar()

function bar_on_double = -> 1.0: bar()

function override_spam_on_struct = -> MyStruct(1): spam()

function main = |args| {
  require(foo_on_string() == "Str.foo", "err")
  require(bar_on_string() == "Bar1.bar", "err")
  require(foo_on_int() == "Obj.foo", "err")
  require(bar_on_int() == "Bar2.bar", "err")
  require(spam_on_int() == "Obj.spam", "err")
  require(foo_on_struct() == "Obj.foo", "err")
  require(bar_on_struct() == "Bar1.bar", "err")
  require(bar_on_double() == "Bar2.bar", "err")
  require(override_spam_on_struct() == "MyStruct.spam", "err")
}
