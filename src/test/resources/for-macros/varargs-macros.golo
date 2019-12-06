
module golo.test.VarargsMacros

import gololang.ir
import gololang.ir.DSL

macro foo = |a, b, c...| {
  let col = CollectionLiteral.create("list", a, b)
  foreach (elt in c) {
    col: add(elt)
  }
  return col
}

#tag::answer[]
macro answer = -> constant(42)
#end::answer[]

#tag::sayHellos[]
macro sayHellos = |lst, names...| -> block(array[
  invoke("add"): withArgs(plus(constant("Hello "), name)): on(lst)
  foreach name in names
])
#end::sayHellos[]
