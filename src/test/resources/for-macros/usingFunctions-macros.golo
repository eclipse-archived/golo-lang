
module golo.test.UsingFunctionsMacros

import gololang.ir
import gololang.ir.DSL

macro printHello = |name| -> match {
  when check(name) then constant(":)")
  otherwise plus(constant("Hello "), name)
}

function check = |name| -> name oftype ConstantStatement.class and name: value() == "World"

macro answer = -> call("foo")

macro answer2 = -> call("who")

function foo = -> 42
