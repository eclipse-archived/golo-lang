# Structures

You will never find classes in Golo (you can use the classes defined in Java, but we'll talk about that later).
But don't worry, Golo offers you various means to do without classes, especially Structures.

## Define a structure

_Structure are useful to store data when the set of named entries is fixed._

> **01-structures.golo**
```golo
module hello.structure

# you can define structures in external modules

struct Human = { # 1️⃣
 firstName, # 2️⃣
 lastName # 2️⃣
}

function main = |args| {

  let jane = Human("Jane", "Doe") # 3️⃣
  # or let jane = Human(): firstName("Jane"): lastName("Doe")
  println(jane) # 4️⃣

  let john = Human() # 3️⃣
  println(john) # 5️⃣
  john: firstName("John") # 6️⃣
  john: lastName("Doe") # 6️⃣
  println(john) # 7️⃣

  println("Hello I'm " + jane: firstName() + " " + jane: lastName()) # 8️⃣

}
```

> - 1️⃣ define the structure with the `struct` keyword outside the body of the function
> - 2️⃣ fields of the structure
> - 3️⃣ constructor  of the structure
> - 4️⃣ it will print `struct Human{firstName=Jane, lastName=Doe}`
> - 5️⃣ it will print `struct Human{firstName=null, lastName=null}`
> - 6️⃣ setters
> - 7️⃣ it will print `struct Human{firstName=John, lastName=Doe}`
> - 8️⃣ usage of getters, it will print `Hello I'm Jane Doe`

✋ **To run it**, type this command: `golo golo --files 01-structures.golo`

## Add methods to a structure

Of course, you can augment your structure by adding methods to it 🎉

> **02-structures-augmentations.golo**
```golo
module hello.structure

struct Human = {
 firstName,
 lastName
}

augment Human { # 1️⃣
  function sayHello = |this| { # 2️⃣
    println("Hello I'm " + this: firstName() + " " + this: lastName())
  }

  function saySomething = |self, something| { # 3️⃣
    println(self: firstName() + " " + self: lastName() + " says:" + something)
  }
}

function main = |args| {

  let jane = Human("Jane", "Doe")
  let john = Human("John", "Doe")

  jane: sayHello() # 4️⃣
  john: saySomething("hey! What's up?") # 5️⃣

}
```

> - 1️⃣ add method to the structure with the `augment` block
> - 2️⃣ `this` is a reference to the current structure ...
> - 3️⃣ ... but you use other words instead `this`, like `self`, `that`, `me`, ...
> - 4️⃣ it will print `Hello I'm Jane Doe`
> - 5️⃣ it will print `John Doe says:hey! What's up?`

✋ **To run it**, type this command: `golo golo --files 02-structures-augmentations.golo`

## Named augmentations

_It is possible for augmentations to have a name. A named augmentation is a set of functions that can be applied to some classes or structures._

You can think of this like the traits you have in other languages like Groovy, Rust, Scala. Then you can share augmentations between different structures (you see, no need of classes nor inheritance 😉).

> **03-structures-named-augmentations.golo**
```golo
module hello.structure

struct Dog = { name }
struct Cat = { name }

augmentation runnable = { # 1️⃣
  function run = |this| -> println(this: name() + " is running")
  function walk = |this| -> println(this: name() + " is walking")
}

augmentation woofable = { # 1️⃣
  function woof = |this| -> println(this: name() + ": woof woof")
}

augmentation meowable = { # 1️⃣
  function meow = |this| -> println(this: name() + ": meow meow")
}

augment Dog with runnable, woofable # 2️⃣
augment Cat with runnable, meowable # 2️⃣

function main = |args| {

  let kitty = Cat("Kitty")
  kitty: run() # 3️⃣
  kitty: walk() # 4️⃣
  kitty: meow() # 5️⃣

  let wolf = Dog("Wolf")
  wolf: run() # 6️⃣
  wolf: walk() # 7️⃣
  wolf: woof() # 8️⃣
}
```

> - 1️⃣ you can define a named augmentation with an `augmentation` block,
> - 2️⃣ grafting of the augmentations
> - 3️⃣ it will print `Kitty is running`
> - 4️⃣ it will print `Kitty is walking`
> - 5️⃣ it will print `Kitty: meow meow`
> - 6️⃣ it will print `Wolf is running`
> - 7️⃣ it will print `Wolf is walking`
> - 8️⃣ it will print `Wolf: woof woof`

✋ **To run it**, type this command: `golo golo --files 03-structures-named-augmentations.golo`


ℹ️ more about structures: https://golo-lang.org/documentation/next/#_structs

**This is the end of the "Structures' tutorial"**. You can retrieve the source code here: [resources/structures](resources/structures).

Now, let's have a look to the 😍 lovable **[DynamicObjects](dynamicobjects.md)**
