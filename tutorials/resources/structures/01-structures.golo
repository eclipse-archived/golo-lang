module hello.structure

# you can define structures in external modules

struct Human = {
 firstName,
 lastName
}

function main = |args| {

  let jane = Human("Jane", "Doe")
  # or let jane = Human(): firstName("Jane"): lastName("Doe")
  println(jane)

  let john = Human()
  println(john)
  john: firstName("John")
  john: lastName("Doe")
  println(john)

  println("Hello I'm " + jane: firstName() + " " + jane: lastName())

}
