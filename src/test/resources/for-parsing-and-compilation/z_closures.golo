module Closures

function a = {
  let f = |a| { println(a) }
}

function b = {
  return |i| { return i * 2 }
}

function c = {
  return {
    return 123
  }
}
