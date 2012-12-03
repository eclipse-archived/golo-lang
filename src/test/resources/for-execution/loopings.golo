module golotest.execution.Loopings

function times = |n| {
  var times = 0
  while (times < n) { times = times + 1 }
  return times
}

function fact = |value, n| {
  if n >= 0 {
    var result = 1
    for (var i = 0, i < n, i = i + 1) {
      result = result * value
    }
    return result
  }
  return null
}

function concat_to_string = |iterable| {
  var result = ""
  foreach (item in iterable) {
    result = result + item
  }
  return result
}
