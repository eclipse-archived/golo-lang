module golotest.execution.Arrays

function make_123 = {
  return array(1, 2, 3)
}

function get_123_at = |index| {
  return a_get(make_123(), index)
}

function array_of = |value| {
  return array(value)
}
