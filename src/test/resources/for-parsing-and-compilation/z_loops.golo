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

function forLoop = {
  var result = 0
  for (var i = 0, i < 10, i = i + 1) {
    result = 0 + i
  }
  return result
}
