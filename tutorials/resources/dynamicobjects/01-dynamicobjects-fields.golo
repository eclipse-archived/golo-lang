module hello.dynamic.objects

function main = |args| {

  let jane = DynamicObject(): firstName("Jane"): lastName("Doe")
  println(jane)

  let john = DynamicObject()
  println(john)
  john: firstName("John")
  john: lastName("Doe")
  println(john)

  println("Hello I'm " + jane: firstName() + " " + jane: lastName())

}
