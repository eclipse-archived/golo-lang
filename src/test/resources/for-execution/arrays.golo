module golotest.execution.Arrays

function make_123 = {
  return array[1, 2, 3]
}

function get_123_at = |index| {
  return make_123(): get(index)
}

function array_of = |value| {
  return array[value]
}

function array_of_doubles = {
  return array[123.0, -123.0, 123.456, 123.0e3, 1_234.0_10e-3]
}

function array_of_floats = {
  return array[123.0_F, -123.0_F, 123.456_F, 123.0e3_F, 1_234.0_10e-3_F]
}

function array_of_big_decimals = {
  return array[123.0_B, -123.0_B, 123.456_B, 123.0e3_B, 1_234.0_10e-3_B]
}

function array_of_big_integers = {
  return array[123_B, -123_B, 1_234_B, -1_234_B]
}

function as_list = {
  return java.util.Arrays.asList(1, 2, 3)
}

function get_method = {
  let a = array[1, 2, 3]
  return a: get(0)
}

function set_method = {
  let a = array[1, 2, 3]
  a: set(0, 10)
  return a: get(0)
}

function length_method = {
  return array[1, 2, 3]: length()
}

function size_method = {
  return array[1, 2, 3]: size()
}

function iterator_method = {
  let a = array[1, 2, 3]
  var sum = 0
  foreach (i in a) {
    sum = sum + i
  }
  return sum
}

function toString_method = {
  return array[1, 2, 3]: toString()
}

function equals_method = {
  return array[1, 2, 3]: equals(array[1, 2, 3])
}

function asList_method = {
  return array[1, 2, 3]: asList()
}

function getClass_method = {
  return array[1, 2, 3]: getClass()
}

function head_method = {
  return array[1, 2, 3]: head()
}

function tail_method = {
  return array[1, 2, 3]: tail(): asList()
}

function head_method_empty = {
  return array[]: head()
}

function isEmpty_method = {
  return array[]: isEmpty()
}

function tail_method_empty = {
  return array[]: tail(): equals(array[])
}

function last_method_empty = {
  try {
    return array[]: last()
  } catch (e) {
    if e oftype java.util.NoSuchElementException.class {
      return
    }
    throw e
  }
  raise("must fail")
}

function last_method = {
  return array[1, 2, 3]: last()
}

function first_method_empty = {
  try {
    array[]: first()
  } catch (e) {
    if e oftype java.util.NoSuchElementException.class {
      return
    }
    throw e
  }
  raise("must fail")
}

function first_method = {
  return array[1, 2, 3]: first()
}

function contains_index = {
  let a = array[1, 'a', "plop"]
  return array[
    a: contains('a'),
    a: contains('z'),
    a: indexOf("plop"),
    a: indexOf("foo")]
}
