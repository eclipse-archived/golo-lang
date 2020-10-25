module hello.dynamic.objects

function main = |args| {

  let jane = DynamicObject()
    : firstName("Jane")
    : lastName("Doe")
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

  jane: hello()
  jane: message("👋 hello world 🌍")

  jane: about(|this| -> "I'm " + this: firstName())
  println(jane: about())
}
