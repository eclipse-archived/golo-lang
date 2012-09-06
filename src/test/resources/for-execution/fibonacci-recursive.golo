module golotest.execution.Fibonacci

function fib = |n| {
  require(n >= 0, "n should >= 0")
  if n <= 1 {
    return n
  } else {
    return fib(n - 1) + fib(n - 2)
  }
}
