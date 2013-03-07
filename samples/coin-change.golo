module CoinChange

import java.util.LinkedList

function change = |money, coins| -> match {
  when money == 0 then 1
  when (money < 0) or (coins: isEmpty()) then 0
  otherwise change(money - coins: head(), coins) + change(money, coins: tail())
}

function main = |args| {
  let coins = LinkedList(): append(1, 2, 5, 10, 20)
  println("Coins: " + coins)
  println("0: " + change(0, coins))
  println("1: " + change(1, coins))
  println("2: " + change(2, coins))
  println("10: " + change(10, coins))
  println("12: " + change(12, coins))
  println("6: " + change(6, coins))
}
