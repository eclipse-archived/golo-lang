# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.AsyncHelpers

import gololang.Async
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors

local function fib = |n| {
  if n <= 1 {
    return n
  } else {
    return fib(n - 1) + fib(n - 2)
  }
}

function main = |args| {

  let executor = newCachedThreadPool()
  println("Let's do some useless asynchronous operations...")

  var f = executor: enqueue({
    Thread.sleep(1000_L)
    return 666
  })
  f:
    onSet(|v| -> println(">>> #slow -> " + v)):
    onFail(|e| -> println(">>> #fail -> " + e))
  f:
    cancel(true)

  f = executor: enqueue({
    Thread.sleep(1000_L)
    return 666
  })
  f:
    onSet(|v| -> println(">>> #ok -> " + v)):
    onFail(|e| -> println(">>> #wtf? -> " + e))

  let fib_10 = promise()
  let fib_20 = promise()
  let fib_30 = promise()
  let fib_40 = promise()

  let futures = [
    fib_10: future(), fib_20: future(),
    fib_30: future(), fib_40: future()
  ]

  executor: submit(-> fib_10: set(fib(10)))
  executor: submit(-> fib_20: set(fib(20)))
  executor: submit(-> fib_30: set(fib(30)))
  executor: submit(-> fib_40: set(fib(40)))

  all(futures): onSet(|results| -> println(">>> Fibs: " + results))

  let truth = promise()
  truth:
    future():
    map(|v| -> "truth=" + v):
    onSet(|v| -> executor: submit(-> println(">>> (another thread) " + v))):
    onSet(|v| -> println(">>> (same thread) " + v))
  executor: submit({
    Thread.sleep(500_L)
    truth: set(42)
  })

  Thread.sleep(1000_L)
  executor: shutdown()
  executor: awaitTermination(2_L, SECONDS())
  println("Bye!")
}
