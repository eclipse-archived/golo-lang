module golotest.Variables

import java.lang.System

function now = {
  let currentTime = nanoTime()
  var foo = "foo"
}

function now2 = |a, b| {
  let currentTime = nanoTime()
  var foo = "foo"
  var c = a
  var d = b
  d = foo
}
