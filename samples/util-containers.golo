# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module MoreCoolContainers

function main = |args| {

  println(">>> DynamicVariable")
#tag::dynamicvariable[]
  let dyn = DynamicVariable("Foo")
  println(dyn: value())

  let t1 = Thread({
    dyn: withValue(666, {
      println(dyn: value())
    })
  })

  let t2 = Thread({
    dyn: withValue(69, {
      println(dyn: value())
    })
  })

  t1: start()
  t2: start()
  t1: join()
  t2: join()
  println(dyn: value())
#end::dynamicvariable[]

  println(">>> Observable")
#tag::observable[]
  let foo = Observable("Foo")
  foo: onChange(|v| -> println("foo = " + v))

  let mapped = foo: map(|v| -> v + "!")
  mapped: onChange(|v| -> println("mapped = " + v))

  foo: set("69")
#end::observable[]
}
