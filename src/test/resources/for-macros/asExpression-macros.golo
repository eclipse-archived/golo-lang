
module golo.test.AsExpressionMacros

import gololang.ir
import gololang.ir.DSL

macro answer = -> constant(42)

macro answer2 = -> call("foo")
