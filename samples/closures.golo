module Closures

local function sayHello = |who| -> "Hello " + who + "!"

function main = |args| {
  let adder = |a, b| -> a + b
  println(adder: invokeWithArguments(1, 2))
  println(adder(1, 2))

  let addToTen = adder: bindTo(10)
  println(addToTen: invokeWithArguments(2))
  println(addToTen(2))

  println(sayHello("Julien"))

  let list = java.util.LinkedList()
  let pump_it = {
    list: add("I heard you say")
    list: add("Hey!")
    list: add("Hey!")
  }
  pump_it()
  println(list)
}
