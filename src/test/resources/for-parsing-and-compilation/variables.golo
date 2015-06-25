module golotest.Variables

import java.lang.System

function now = {
  let currentTime = nanoTime()
  var foo = "foo"
}

function now2 = |a, b| {
  let currentTime = nanoTime()
  var foo =
            "foo"
  var c =
            a
  var d = b
  d =
            foo
}

function long_number = {
  return 100_L
}

function string_class = { return String.class }

function a_char = {
  return 'a'
}
