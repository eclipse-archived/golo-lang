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
    for (var i = 0, i < n, i = i + 1) {
      result = result * value
    }
    return result
  }
  return null
}

