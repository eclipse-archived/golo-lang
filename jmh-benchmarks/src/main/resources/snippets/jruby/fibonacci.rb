def fib(n)
  if n <= 2
    1
  else
    fib(n - 1) + fib(n - 2)
  end
end