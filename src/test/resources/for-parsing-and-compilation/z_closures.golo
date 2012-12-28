module Closures

function a = {
  let f = |a| { println(a) }
}

function b = |factor| {
  return |i| { return i * factor }
}

function c = {
  return {
    return 123
  }
}
