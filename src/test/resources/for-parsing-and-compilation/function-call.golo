module FunctionCall

import java.lang.System

function callPrintln = |useless, parameters| {
  System.out.println("golo golo!")
}

local function foo = |boo| { }
local function bar = |bzz| { }

local function go = {
  callPrintln(foo(6), bar(9))
}

function someKindOfMain = |args| {
  go()
}
