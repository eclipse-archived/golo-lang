module golotest.execution.Lambdas

function sum_it = ->
  list[1, 2, 3, 4, 5]:
    stream():
    filter(|n| -> n % 2 == 0):
    map(|n| -> n * 10):
    reduce(0, |acc, next| -> acc + next)
