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

function d = {
  return 1 + 2 + 3 + 4 + 5 * 2 * 3 * 4 * -5
}

function e = {
  return (1 + 2) * (3 + 4)
}

function f = {
  return (1 + 2) + (3 + 4) + (5 * 6)
}

function g = {
  return ((1 + 2 + 3) * (5) * 6 + (10 / 2) - 1) / 2
}
