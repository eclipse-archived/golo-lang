module Exceptions

function throwing = {
  throw
    java.lang.RuntimeException("This is a RuntimeException")
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

function inside_closure = |args| {
  let obj = DynamicObject():
    define("setField", |this, fieldName, value| {
      this: model(): fields(): put(fieldName, value)
      try {
        println("A")
      } catch(e) {
        println("B")
      } finally{
        return this
      }
    })
}
