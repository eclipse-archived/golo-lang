module golotest.SimpleReturns

import java.lang.System

function nop = { }

function voider = { return }

local function ignore_me = { }

function chuck = { return 42 }

function ignore_args = |a, b, c| {
  return 666
}

function hello_world =
{
  return "Hello, world!"
}
