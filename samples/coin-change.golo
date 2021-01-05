# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module CoinChange

function change = |money, coins| -> match {
  when money == 0 then 1
  when (money < 0) or (coins: isEmpty()) then 0
  otherwise change(money - coins: head(), coins) + change(money, coins: tail())
}

function main = |args| {
  let coins = list[1, 2, 5, 10, 20]
  let goals = list[0, 1, 2, 10, 12, 6]
  println("Coins: " + coins)
  foreach goal in goals {
    println(goal + ": " + change(goal, coins))
  }
}
