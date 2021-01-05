# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.CollectionLiterals

local function play_with_tuples = {
  let hello = ["Hello", "world", "!"]
  foreach str in hello {
    print(str + " ")
  }
  println("")

  println(hello: get(0) + "-" + hello: get(1) + "-" + hello: get(2))

  println(hello: join("/"))
}

local function play_with_literals = {
  let data = [
    [1, 2, 3],
    tuple[1, 2, 3],
    array[1, 2, 3],
    set[1, 2, 3, 3, 1],
    map[
      ["a", 10],
      ["b", 20]
    ],
    vector[1, 2, 3],
    list[1, 2, 3]
  ]

  data: each(|element| {
    println(element: toString())
    println("  type: " + element: getClass())
  })
}

function main = |args| {
  println(">>> Literals")
  play_with_literals()
  println("\n>>> Tuples")
  play_with_tuples()
}
