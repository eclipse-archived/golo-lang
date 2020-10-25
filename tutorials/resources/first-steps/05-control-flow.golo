module hello.world

function main = |args| {

  # === if then else ===
  let value = 5

  if value <= 5 {
    println("value is <= 5")
  } else {
    println("value is > 5")
  }

  # === case when ===
  # the case construction is used inside a closure or a function
  let choice = |value| {
    case {
      when value == "one" {
        return "ONE"
      }
      when value == "two" {
        return "TWO"
      }
      otherwise {
        return "NOT IN THE LIST"
      }
    }
  }

  println (choice("two"))
  println (choice("three"))

  # you can use the case as a statement
  let another_value = "one"
  case {
      when another_value == "one" {
          println("ONE")
      }
      when another_value == "two" {
          println("TWO")
      }
      otherwise {
          println("NOT IN THE LIST")
      }
  }

  # === match when then ===
  let your_choice = "one"

  let and_the_result_is = -> match {
    when your_choice == "one" then "ONE"
    when your_choice == "two" then "TWO"
    otherwise "NOT IN THE LIST"
  }
  # The values to be returned are specified after a then keyword that follows a boolean expression to be evaluated.

  println(and_the_result_is())

  # === Loops ===

  # while
  var counter = 0
  while (counter < 10) {
    counter = counter + 1
  }
  println(counter)

  # for
  counter = 0
  for (var i = 0, i <= 10, i = i + 1) {
    counter = i
  }
  println(counter)

}


