module golotest.execution.VariableAssignments

import java.lang
import org.eclipse.golo.compiler.testing.support.GoloTestHelperFunctions

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

function string_class = {
  return java.lang.String.class
}

function string_class_from_package_import = {
  return String.class
}

function is_same_ref = |a, b| {
  return `is(a, b)
}

function a_char = {
  return 'a'
}
