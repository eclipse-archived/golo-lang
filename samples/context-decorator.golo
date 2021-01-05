# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.ContextDecorator

import gololang.Decorators

let myContext = defaultContext():
  count(0):
  define("entry", |this, args| {
    this: count(this: count() + 1)
    println("hello:" + this: count())
    return args
  }):
  define("exit", |this, result| {
    require(result >= 3, "wrong value")
    println("goobye")
    return result
  }):
  define("catcher", |this, e| {
    println("Caught " + e)
    throw e
  }):
  define("finallizer", |this| {println("do some cleanup")})


@withContext(myContext)
function foo = |a, b| {
  println("Hard computation")
  return a + b
}

function main = |args| {
  println(foo(1,2))
  println("====")
  println(withContext(myContext)(|a| -> 2*a)(3))
  println("====")
  try {
    println(foo(1, 1))
  } catch (e) { }
}
