module loops

function forever = {
  while true { }
}

function loop = |n| {
  var times = 0
  while times < n {
    times = times + 1
  }
  return times
}

function withJunk = {
  var result = 0
  for (var i = 0, i < 10, i = i + 1) {
    let foo = i
    if foo == 3 {
      let bar = "bar"
    }
    result = 0 + i
  }
  return result
}

function fact = |value, n| {
  if n >= 0 {
    var result = 1
    for (
      var i = 0,
      i < n,
      i = i + 1
    ) {
      result = result * value
    }
    return result
  }
  return null
}

function loop_with_call = {
  while true {
    forever()
  }
}

function z_list_with_ids = {
  let list = java.util.LinkedList()
  for (var i = 0, i < 10000, i = i + 1) {
    list: add("#" + i)
  }
  return list
}

function for_each = |iterable| {
  foreach (elem in iterable) {
    println(elem)
  }
}

function for_each_guarded = |iterable| {
  foreach elem in iterable when elem < 10 {
    println(elem)
  }
}
