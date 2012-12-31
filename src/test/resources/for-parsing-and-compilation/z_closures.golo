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

function d = {
  let foo = |plop| {
    return plop + "!"
  }
  for (var i = 0, i < 10, i = i + 1) {
    foo(i)
  }
}

function compact = |a| -> a

function e = {
  let foo = 3
  return |x| -> x + foo
}

function f = |a| {
  return |x| -> a + x
}

function g = |a, b| -> a + b
