# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module Closures

local function sayHello = |who| -> "Hello " + who + "!"

function main = |args| {
  let adder = |a, b| -> a + b
  println(adder: invoke(1, 2))
  println(adder(1, 2))

  let addToTen = adder: bindTo(10)
  println(addToTen: invoke(2))
  println(addToTen(2))

  let adding = |x| -> |y| -> adder(x, y)
  let addingTen = adding(10)
  println(addingTen(4))
  println(adding(2)(4))

  println(sayHello("Julien"))


#tag::pump_it[]
  let list = java.util.LinkedList()
  let pump_it = {
    list: add("I heard you say")
    list: add("Hey!")
    list: add("Hey!")
  }
  pump_it()
  println(list)
#end::pump_it[]
}
