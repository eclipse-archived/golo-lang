module Closures

function a = {
  let g = |value| {
    let x = value
    println(x)
  }
  return g(2)
}

function b = |factor| {
  return |i| { return i * factor }
}

function c = {
  return {
    return 123
  }
}
