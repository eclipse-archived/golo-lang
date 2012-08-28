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
