module golotest.SimpleReturns

import java.lang.System

function nop = { }

function voider = { return }

local function ignore_me = { }

function chuck = { return 42 }

function ignore_args = |a, b, c| {
  return 666
}

function yes = { return true }

function no = { return false }

function hello_world = {
  return "Hello, world!"
}

function doubles = {
  let a = 123.45
  let b = -123.45
  let c = 123.4e5
  let d = 1_234.000_123e-5
}

function floats = {
  let a = 123.45_F
  let b = -123.45_F
  let c = 123.4e5_F
  let d = 1_234.000_123e-5_F
}

function big_decimals = {
  let a = 123.45_B
  let b = -123.45_B
  let c = 123.4e5_B
  let d = 1_234.000_123e-5_B
}

function escaped = {
  return "\nFoo\r\n"
}
