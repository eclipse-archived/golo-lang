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
