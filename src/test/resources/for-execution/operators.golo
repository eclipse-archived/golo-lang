module golotest.execution.Operators

function plus_one = |a| {
  return a + 1
}

function minus_one = |a| {
  return a - 1
}

function half = |a| {
  return a / 2
}

function twice = |a| {
  return a * 2
}

function compute_92 = {
  # Note: the parser is wrong on:
  #   ((1 + 2 + 3) * (5) * 6 + (10 / 2) - 1) / 2
  #                          ^
  #                          not seen, the AST is all under the `-` symbol
  return ((1 + 2 + 3) * (5) * 6 + ((10 / 2) - 1)) / 2
}
