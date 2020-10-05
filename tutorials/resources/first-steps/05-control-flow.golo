module hello.world

function main = |args| {

  let value = 5

  if value <= 5 {
    println("value is <= 5")
  } else {
    println("value is > 5")
  }

  # the case construction can only be used inside a closure or a function
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

  # The values to be returned are specified after a then keyword that follows a boolean expression to be evaluated.

  let your_choice = "one"

  let and_the_result_is = -> match {
    when your_choice == "one" then "ONE"
    when your_choice == "two" then "TWO"
    otherwise "NOT IN THE LIST"
  }

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


