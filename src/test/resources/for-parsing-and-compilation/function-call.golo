module FunctionCall

function now = |useless, parameters| {
  return java.lang.System.nanoTime()
}

local function foo = |boo| { }
local function bar = |bzz| { }

local function go = {
  return now(foo(6), bar(9))
}

function someKindOfMain = |args| {
  go()
}

function var_arg_ed = |index, args...| {
  return aget(args, index)
}
