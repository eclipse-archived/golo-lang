# Reported by Sylvain Desgrais
# https://github.com/golo-lang/golo-lang/issues/216
module bug

var counter = 0

function func = {
  counter = counter + 1
  return {
    counter = counter + 1 # try to access to an enclosed variable
  }
}

function main = |args| {
  func()()
}