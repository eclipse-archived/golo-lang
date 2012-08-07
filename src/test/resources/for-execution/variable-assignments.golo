module golotest.execution.VariableAssignments

import gololang.compiler.testing.support.GoloTestHelperFunctions

function echo = |what| { return what }

function echo_middleman = |what| {
  var temp1 = null
  temp1 = what
  let temp2 = temp1
  return temp2
}

function greet = |someone| {
  let exclamation = "!"
  return concatenate(concatenate("Hello ", someone), exclamation)
}
