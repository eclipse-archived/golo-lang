module golotest.execution.SamSupport

import fr.insalyon.citi.golo.compiler.testing.support

function func = ->
  SamSupportHelpers.apply(|e| -> e + e)

function ctor = ->
  SamSupportHelpers(|e| -> e + "!"): state()

function meth = ->
  SamSupportHelpers(|e| -> e + "!"): plopIt(|e| -> e, "Yeah")

function func_varargs = ->
  SamVarargsSupportHelpers.apply(|e| -> e: get(0) + e: get(1))

function ctor_varargs = ->
  SamVarargsSupportHelpers(|e| -> e: get(0) + e: get(1) + "!"): state()

function meth_varargs = ->
  SamVarargsSupportHelpers(|e| -> e: get(0) + e: get(1) + "!"): plopIt(|e| -> e: get(0) + e: get(1), "Yeah")