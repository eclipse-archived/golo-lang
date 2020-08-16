
module golo.test.SideeffectMacros

import gololang.ir
import gololang.ir.DSL

#tag::sideeffect[]
macro sideeffect = |c| {
  require(c oftype ConstantStatement.class,
    "The `sideeffect` macro must be called with a constant statement")
  let operation = c?: parent()?: parent()
  require(operation oftype BinaryOperation.class,
    "The `sideeffect` macro must be called inside a binary operation")
  operation: left()?: replaceInParentBy(constant(40))
  c: value(c: value() + 1)
  return c
}
#end::sideeffect[]
