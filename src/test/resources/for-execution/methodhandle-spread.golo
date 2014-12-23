module golotest.execution.MethodHandleSpread

function spread_args = |x| {
  return |x| {
    return x
  }: spread(array[x])
}

function spread_empty = {
  return {
    return 42
  }: spread(array[])
}

function spread_vargs = |x| {
  return |x...| {
    return x: get(0)
  }: spread(array[array[x, 1, 2]])
}
