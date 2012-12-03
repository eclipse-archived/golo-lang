module golotest.execution.Arrays

function make_123 = {
  return Array(1, 2, 3)
}

function get_123_at = |index| {
  return aget(make_123(), index)
}

function array_of = |value| {
  return Array(value)
}

function array_of_doubles = {
  return Array(123.0, -123.0, 123.456, 123.0e3)
}

function array_of_floats = {
  return Array(123.0_F, -123.0_F, 123.456_F, 123.0e3_F)
}

function as_list = {
  return toCollection(Array(1, 2, 3))
}
