module golotest.execution.CallJavaObjects

import java.lang
import java
import java.util.Arrays

function new_integer = {
  return java.lang.Integer("666")
}

function new_integer_from_imports = {
  return Integer("666")
}

function make_a_list = {
  return util.LinkedList(asList(1, 2, 3))
}

function make_another_list = {
  return util.LinkedList(util.Arrays.asList(1, 2, 3))
}

function make_another_list_from_array = {
  return util.LinkedList(util.Arrays.asList(array[1, 2, 3]))
}
