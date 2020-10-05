module hello.world

function main = |args| {

  let say_hello = -> println("Hello") # I know it's not pure 😝

  say_hello()

  let say_hello_to_somebody = |name| {
    println("Hello " + name)
  }

  say_hello_to_somebody("Bob")

  let sum = |a, b| -> a + b

  println(
    sum(10, sum(30, 2))
  )

}

