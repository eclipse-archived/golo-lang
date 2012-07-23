module golotest.execution.ParameterLessFunctionCalls

function now = {
  return java.lang.System.nanoTime()
}

function hello = {
  return "hello()"
}

function call_hello = {
  return hello()
}

function call_now = {
  return now()
}
