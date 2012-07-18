module FunctionCall

import java.lang.System

function callPrintln = |useless, parameters| {
  System.out.println("golo golo!")
}

function someKindOfMain = |args| {
  callPrintln()
}
