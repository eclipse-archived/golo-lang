module golotest.execution.ParameterLessFunctionCalls

# Most imports are useless, on purpose!
import java.util.List
import java.lang.Class
import java.lang.Object
import java.lang.System

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

function call_nanoTime = {
  return nanoTime()
}

function nil = {
  return null
}

function sysOut = {
  return out()
}

function five = {
  return java.lang.Integer.toString(5)
}
