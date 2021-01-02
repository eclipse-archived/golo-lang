module golo.test.MetaUtilsMacros

import gololang.ir.DSL

macro makeTest = |name, fn| {
  fn: returns(constant(fn: metadata(name: value())))
  return fn
}
