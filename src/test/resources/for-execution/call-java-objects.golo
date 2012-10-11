module golotest.execution.CallJavaObjects

import java.lang

function new_integer = {
  return java.lang.Integer("666")
}

function new_integer_from_imports = {
  return Integer("666")
}
