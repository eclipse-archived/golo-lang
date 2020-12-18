module golotest.execution.Loopings

#tag::times[]
function times = |n| {
  var times = 0
  while (times < n) { times = times + 1 }
  return times
}
#end::times[]

#tag::fact[]
function fact = |value, n| {
  if n < 0 { return null }
  var result = 1
  for (var i = 0, i < n, i = i + 1) {
    result = result * value
  }
  return result
}
#end::fact[]

#tag::concat[]
function concat_to_string = |iterable| {
  var result = ""
  foreach (item in iterable) {
    result = result + item
  }
  return result
}
#end::concat[]

#tag::guarded[]
function foreach_guarded = |iterable| {
  var result = ""
  foreach item in iterable when item >= 10 {
    result = result + item
  }
  return result
}
#end::guarded[]

function break_continue = {
#tag::break_continue[]
  var i = 0
  let s = StringBuilder()
  while true {
    i = i + 1
    if i < 40 {
      continue
    } else {
      s: append(i): append(" ")
    }
    if i == 50 {
      break
    }
  }
  s: append("bye")
  require(s: toString() == "40 41 42 43 44 45 46 47 48 49 50 bye", "err")
#end::break_continue[]
}

