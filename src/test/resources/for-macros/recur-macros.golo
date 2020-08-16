
module golo.test.ReccurMacros

import gololang.ir
import gololang.ir.DSL

macro sum = |x| -> match {
    when x: value() <= 1 then constant(1)
    otherwise plus(x, macroCall("sum"): withArgs(y))
  } with {
    y = constant(x: value() - 1)
  }

macro sum2 = |x| -> match {
    when x: value() <= 1 then constant(1)
    otherwise plus(x, sum2(y))
  } with {
    y = constant(x: value() - 1)
  }
