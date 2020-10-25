module hello.structure

# you can define structures in external modules

struct Human = {
 firstName,
 lastName
}

augment Human {
  function sayHello = |this| {
    println("Hello I'm " + this: firstName() + " " + this: lastName())
  }

  function saySomething = |self, something| {
    println(self: firstName() + " " + self: lastName() + " says:" + something)
  }
}

function main = |args| {

  let jane = Human("Jane", "Doe")
  let john = Human("John", "Doe")

  jane: sayHello()
  john: saySomething("hey! What's up?")

}
