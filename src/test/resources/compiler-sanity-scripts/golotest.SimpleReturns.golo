module golotest.SimpleReturns

import java.lang.System

function nop = { }

function voider = { return }

function chuck = { return 42 }

function ignore_args = |a, b, c| {
  return 666
}

function hello_world =
{
  return "Hello, world!"
}
