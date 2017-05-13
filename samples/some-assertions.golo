# Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module golo.samples.Assertions

import gololang.Assertions

struct Point = {
  x, y
}

function main = |args| {

  assert(-> 5: equals(5))

  assert(-> "BoB" oftype String.class, |error| {
    println("Huston?")
  })

  assert(predicate= -> "BoB" oftype Integer.class,
    successMessage= "This is an Integer",
    onSuccess= |res| {
      println("You Win")
    },
    errorMessage= "This isn't an Integer",
    onError = |error| {
      println("Huston?")
    }
  )

  assert(predicate= -> 5 < 0, onSuccess=|res| {}, onError=|err| {
    println("5 is not less than 0!")
  })

  assertEqual(42, 42)

  assertEqual(42, 69, |error| {
    println(error)
  })

  assertEqual(Point(5,5), Point(5,5), |error| {
    println("???")
  })

  let point = Point(5,5)
  assertEqual(point: frozenCopy(), point: frozenCopy(), |res| {
      println(":)")
    },
    |error| {
      println(":(")
  })

  gololang.Assertions.displayTestsReport()
}