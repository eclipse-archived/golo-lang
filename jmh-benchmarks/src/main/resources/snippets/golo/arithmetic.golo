module Arithmetics

function gcd = |x, y, repeat| {
  var res = 0_L
  for (var i = 0_L, i < repeat, i = i + 1_L) {
    var a = x
    var b = y
    while a != b {
      if a > b {
        a = a - b
      } else {
        b = b - a
      }
    }
    res = a
  }
  return res
}

function sum = |x, y| -> x + y
