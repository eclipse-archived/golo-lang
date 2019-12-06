module golo.test.MacroWithInit2

import gololang.ir.DSL

@contextual
macro init = |this, args...| {
  this: enclosingModule()?: addFunction(
    `function(args: get(0): name()): withParameters("a", "b")
      : returns(plus(refLookup("a"), refLookup("b"))))
}
