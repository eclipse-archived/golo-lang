module Fibonacci

function fib = |n| {
  if n <= 2_L {
    return 1_L
  } else {
    return fib(n - 1_L) + fib(n - 2_L)
  }
}

