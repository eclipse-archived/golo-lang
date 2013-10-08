module FunctionCall

function now = |useless, parameters| {
  return java.lang.System.nanoTime()
}

local function foo = |boo| { }
local function bar = |bzz| { }

local function go = {
  return now ( # Damn
    foo(6),    # This
    bar(9)     # Is
  )            # Awesome
}

function someKindOfMain = |args| {
  go()
  `is()
  `isnt()
  `not()
}

function var_arg_ed = |index, args...| {
  return aget(args,
              index)
}

function functional = -> foo(1)(2)(3)
