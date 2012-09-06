module samples.Fibonacci

import java.lang.System

function fib = |n| {
  if n <= 1 {
    return n
  } else {
    return fib(n - 1) + fib(n - 2)
  }
}

local function run = {
  let start = System.currentTimeMillis()
  let result = fib(40)
  let duration = System.currentTimeMillis() - start
  println(">>> " + result + " (took " + duration + "ms)")
}

function main = |args| {
  while true {
    run()
  }
}

