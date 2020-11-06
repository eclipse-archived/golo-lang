module hello.structure

# you can define structures in external modules

struct Dog = { name }
struct Cat = { name }

augmentation runnable = {
  function run = |this| -> println(this: name() + " is running")
  function walk = |this| -> println(this: name() + " is walking")
}

augmentation woofable = {
  function woof = |this| -> println(this: name() + ": woof woof")
}

augmentation meowable = {
  function meow = |this| -> println(this: name() + ": meow meow")
}

augment Dog with runnable, woofable
augment Cat with runnable, meowable

function main = |args| {

  let kitty = Cat("Kitty")
  kitty: run()
  kitty: walk()
  kitty: meow()

  let wolf = Dog("Wolf")
  wolf: run()
  wolf: walk()
  wolf: woof()
}
