# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.Adapters

local function list_sample = |fabric| {
  println(">>> list_sample()")
  let carbonCopy = list[]
  let conf = map[
    ["extends", "java.util.ArrayList"],
    ["overrides", map[
      ["*", |super, name, args| {
        if name == "add" {
          if args: length() == 2 {
            carbonCopy: add(args: get(1))
          } else {
            carbonCopy: add(args: get(1), args: get(2))
          }
        }
        return super: invoke(args)
      }
    ]]
  ]]
  let list = fabric: maker(conf): newInstance()
  list: add("bar")
  list: add(0, "foo")
  list: add("baz")
  println("      list: " + list + " " + list: getClass())
  println("carbonCopy: " + carbonCopy + " " + carbonCopy: getClass())
}

local function runnable_sample = |fabric| {
  println(">>> runnable_sample")
  let result = array[1, 2, 3]
  let conf = map[
    ["interfaces", ["java.io.Serializable", "java.lang.Runnable"]],
    ["implements", map[
      ["run", |this| {
        for (var i = 0, i < result: length(), i = i + 1) {
          result: set(i, result: get(i) + 10)
        }
      }]
    ]]
  ]
  let runner = fabric: maker(conf): newInstance()
  runner: run() # <1>
  println("      result: " + result: toString())
  println("serializable? " + (runner oftype java.io.Serializable.class))
  println("    runnable? " + (runner oftype java.lang.Runnable.class))
}

function main = |args| {
  let fabric = AdapterFabric()
  list_sample(fabric)
  runnable_sample(fabric)
}
