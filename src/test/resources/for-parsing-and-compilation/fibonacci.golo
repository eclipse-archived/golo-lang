module Fibonacci

function fib = |n| {
  if n <= 1 {
    return n
  } else {
    return fib(n - 1) + fib(n - 2)
  }
}

function fib_memoized = |n, mem| {
  if n <= 1 {
    return n
  } else if mem: containsKey(n) {
    return mem: get(n)
  } else {
    let res = fib(n - 1, mem) + fib(n - 2, mem)
    mem: put(n, res)
    return res
  }
}

