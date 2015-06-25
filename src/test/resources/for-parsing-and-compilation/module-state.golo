module ModuleState

let a = 3

let b = System.currentTimeMillis()

var c = "Plop"

var d = 0

function woot = {
  let diff = System.currentTimeMillis() - b
  d = diff
  return d
}
