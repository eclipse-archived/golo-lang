module golotest.execution.MethodInvocations

function hello = {
  return "Hello": toString()
}

function a_list = |a, b| {
  let list = java.util.LinkedList()
  list: add(a)
  list: add(b)
  return list
}

function str_build = {
  return java.lang.StringBuilder("h"):
    append("e"):
    append("l"):
    append("l"):
    append("o"):
    toString()
}
