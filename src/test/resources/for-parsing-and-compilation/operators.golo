module Operators

function a = {
  return 3 + 2
}

function b = {
  return ((3) + 2)
}

function c = |a, b| {
  let sum = a + b * 4
  return sum * 2 + (sum + 4)
}