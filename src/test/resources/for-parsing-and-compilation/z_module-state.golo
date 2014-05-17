module ModuleState

let a = 3

let b = System.currentTimeMillis()

var c = "Plop"

var d = 0

function woot = {
  d = System.currentTimeMillis() - b
  return d
}
