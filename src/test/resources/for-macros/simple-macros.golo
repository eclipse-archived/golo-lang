
module golo.test.SimpleMacros

import gololang.ir
import gololang.ir.DSL

macro sayHello = |lst, name| {
  return invoke("add"): withArgs(plus(constant("Hello "), name)): on(lst)
}

@contextual
macro create = |this, name| {
  return `function(name: value())
          : returns(constant(name: value()
              + " in "
              + this: parent()?: packageAndClass()))
}

@contextual
macro createLambda = |this, message| {
  return lambda()
  : returns(constant(message: value()
    + " in "
    + this: ancestorOfType(GoloFunction.class)?: name()))
}

#tag::swap[]
macro swap = |a, b| -> block(
  `var(tmp, b),
  assign(b, a),
  assign(a, tmp)) with {
    tmp = localRef()
  }
#end::swap[]

macro answer = -> constant(42)

#tag::dowhile[]
macro doWhile = |condition, body| -> block(
  body,
  `while(condition): block(body)
)
#end::dowhile[]
