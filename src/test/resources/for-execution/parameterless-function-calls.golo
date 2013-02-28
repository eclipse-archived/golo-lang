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

function System_Out = {
  return System.out()
}

function five = {
  print(">>> ")
  println(5)
  return java.lang.Integer.toString(5)
}

function string_class = -> java.lang.String.class
function string_module = -> java.lang.String.module
