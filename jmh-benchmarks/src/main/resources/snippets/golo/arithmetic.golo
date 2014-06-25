module Arithmetics

function gcd = |x, y| {
  var a = x
  var b = y
  while a != b {
    if a > b {
      a = a - b
    } else {
      b = b - a
    }
  }
  return a
}
