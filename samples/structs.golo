# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

#tag::def[]
module StructDemo

struct Point = { x, y }
#end::def[]

augment StructDemo.types.Point {

  function move = |this, offsetX, offsetY| {
    this: x(this: x() + offsetX)
    this: y(this: y() + offsetY)
    return this
  }

  function relative = |this, offsetX, offsetY| -> Point(this: x() + offsetX, this: y() + offsetY)
}

#tag::main[]
function main = |args| {

  let p1 = Point(1, 2)
  let p2 = Point(): x(1): y(2)
  let p3 = p1: frozenCopy()
  let p4 = p1: frozenCopy()

  println("p1 == p2 " + (p1 == p2))
  println("p1 == p3 " + (p1 == p3))
  println("p3 == p4 " + (p3 == p4))

  println("#p1 " + p1: hashCode())
  println("#p2 " + p2: hashCode())
  println("#p3 " + p3: hashCode())
  println("#p4 " + p4: hashCode())
#end::main[]

  println(p1)
  println("x = " + p1: x())
  println("y = " + p1: y())

  println("p1: members() " + p1: members())
  println("p1: values() " + p1: values())
  foreach item in p1 {
    println(item: get(0) + " -> " + item: get(1))
  }

  println("p1: set(\"x\", 10) " + p1: set("x", 10))
  println("p1: move(10, 5) " + p1: move(10, 5))
  println("p1: relative(11, 6) " + p1: relative(11, 6))

  let p5 = ImmutablePoint(10, 20)
  println("p5: " + p5)
  try {
    p5: x(100)
  } catch (expected) {
    println("p5 is immutable, so... " + expected: getMessage())
  }
}
