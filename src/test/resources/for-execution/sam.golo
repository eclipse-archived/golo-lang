module golotest.execution.SamSupport

import fr.insalyon.citi.golo.compiler.testing.support.SamSupportHelpers

function func = ->
  SamSupportHelpers.apply(|e| -> e + e)

function ctor = ->
  SamSupportHelpers(|e| -> e + "!"): state()

function meth = ->
  SamSupportHelpers(|e| -> e + "!"): plopIt(|e| -> e, "Yeah")
