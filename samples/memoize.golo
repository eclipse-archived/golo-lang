# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.MemoizeDecorator

import gololang.Decorators

import java.lang.System

let memo = memoizer()

@memo
function fib = |n| {
  if n <= 1 {
    return n
  } else {
    return fib(n - 1) + fib(n - 2)
  }
}

@memo
function foo = |n| -> n

local function run = {
  let start = System.currentTimeMillis()
  let result = fib(40)
  let duration = System.currentTimeMillis() - start
  println(">>> fib(40) = " + result + " (took " + duration + "ms)")
}

local function run2 = {
  let start = System.currentTimeMillis()
  let result = foo(40)
  let duration = System.currentTimeMillis() - start
  println(">>> foo(40) = " + result + " (took " + duration + "ms)")
}

function main = |args| {
  foreach i in range(0, 5) {
    println("run " + i)
    run()
    run2()
  }
}
