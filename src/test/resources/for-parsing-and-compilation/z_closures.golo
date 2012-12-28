module Closures

function a = {
  let f = |a| {
    let x = a
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
