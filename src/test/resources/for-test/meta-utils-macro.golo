module golo.test.MetaUtilsMacros

import gololang.ir.DSL

macro makeTest = |fn| {
  fn: returns(constant(fn: metadata("themeta")))
  return fn
}
