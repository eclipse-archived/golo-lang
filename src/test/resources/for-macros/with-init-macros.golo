
module golo.test.MacroWithInit

import gololang.ir.Quote

macro init = |p...| -> &quote {
  function plop = -> foo("Hello ", $(p: get(0)))

  function main = |args| {
    println(plop())
  }
}

