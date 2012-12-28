module Closures

function a = {
  let f = |value| {
    let x = value
    println(x)
  }
}

function b = |factor| {
  return |i| { return i * factor }
}

function c = {
  return {
    return 123
  }
}
