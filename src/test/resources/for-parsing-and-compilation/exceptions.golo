module Exceptions

function throwing = {
  throw java.lang.RuntimeException("This is a RuntimeException")
}

function try_catch = {
  try {
    throwing()
  } catch (e) {
    println(e)
  }
}

function try_catch_finally = {
  try {
    throwing()
  } catch (e) {
    println(e)
  } finally {
    println("Finally!")
  }
}

function try_finally = {
  try {
    throwing()
  } finally {
    println("Finally!")
  }
}
