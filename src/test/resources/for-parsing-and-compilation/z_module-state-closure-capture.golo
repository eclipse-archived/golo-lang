# Reported by Sylvain Desgrais
# https://github.com/golo-lang/golo-lang/issues/216
module bug

var counter = 0

function func_1 = {
  counter = counter + 1
  return {
    counter = counter + 1 # try to access to an enclosed variable
    return counter
  }
}

function func_2 = {
  var counter =  1
  return {
    return counter + 1
  }
}


function main = |args| {
  require(func_1()() == 2, "Grrrr 1!")
  require(func_2()() == 2, "Grrrr 2!")
}