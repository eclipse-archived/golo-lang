# Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module Workers

import java.lang.Thread
import java.util.concurrent
import gololang.concurrent.workers.WorkerEnvironment

local function pusher = |queue, message| -> queue: offer(message)

local function generator = |port, message| {
  foreach i in range(0, 100) {
    port: send(message)
  }
}

function main = |args| {

  let env = WorkerEnvironment.builder(): withFixedThreadPool()
  let queue = ConcurrentLinkedQueue()

  let pusherPort = env: spawn(^pusher: bindTo(queue))
  let generatorPort = env: spawn(^generator: bindTo(pusherPort))

  let finishPort = env: spawn(|any| -> env: shutdown())

  foreach i in range(0, 10) {
    generatorPort: send("[" + i + "]")
  }
  Thread.sleep(2000_L)
  finishPort: send("Die!")

  env: awaitTermination(2000)
  println(queue: reduce("", |acc, next| -> acc + " " + next))
}
