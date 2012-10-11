module golotest.execution.CallJavaObjects

import java.lang
import java.util
import java.util.Arrays

function new_integer = {
  return java.lang.Integer("666")
}

function new_integer_from_imports = {
  return Integer("666")
}

function make_a_list = {
  return LinkedList(asList(1, 2, 3))
}
