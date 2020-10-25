module hello.dynamic.objects

function Human = |firstName, lastName| {
  return DynamicObject()
    : firstName(firstName)
    : lastName(lastName)
    : hello(|this| {
        println("Hello I'm " + this: firstName() + " " + this: lastName())
    })
    : message(|this, message| {
        println(
          "I'm " +
          this: firstName() + " " +
          this: lastName() + ": " +
          message
        )
    })
}

function main = |args| {

  let jane = Human("Jane", "Doe")

  jane: hello()
  jane: message("👋 hello world 🌍")

}
