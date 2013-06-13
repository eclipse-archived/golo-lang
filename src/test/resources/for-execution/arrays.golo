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
  return atoList(Array(1, 2, 3))
}

function get_method = {
  let a = Array(1, 2, 3)
  return a: get(0)
}

function set_method = {
  let a = Array(1, 2, 3)
  a: set(0, 10)
  return a: get(0)
}

function length_method = {
  return Array(1, 2, 3): length()
}

function size_method = {
  return Array(1, 2, 3): size()
}

function iterator_method = {
  let a = Array(1, 2, 3)
  var sum = 0
  foreach (i in a) {
    sum = sum + i
  }
  return sum
}

function toString_method = {
  return Array(1, 2, 3): toString()
}

function equals_method = {
  return Array(1, 2, 3): equals(Array(1, 2, 3))
}

function asList_method = {
  return Array(1, 2, 3): asList()
}
