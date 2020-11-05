# Lovely DynamicObjects 😍

_Dynamic objects can have values and methods being added and removed dynamically at runtime._

You can think of this like the `{}` (`object`) of JavaScript (btw, I'm crazy of JavaScript) or the `DynamicObject` class of `.Net`.

## Define a DynamicObject

> **01-dynamicobjects-fields.golo**
```golo
module hello.dynamic.objects

function main = |args| {

  let jane = DynamicObject(): firstName("Jane"): lastName("Doe") # 1️⃣
  println(jane) # 2️⃣

  let john = DynamicObject() # 3️⃣
  println(john) # 4️⃣
  john: firstName("John") # 5️⃣
  john: lastName("Doe") # 5️⃣
  println(john) # 6️⃣

  println("Hello I'm " + jane: firstName() + " " + jane: lastName()) # 7️⃣

}
```

> - 1️⃣ you can define a DynamicObject everywhere and its fields at the same time
> - 2️⃣ it will print `DynamicObject{firstName=Jane, lastName=Doe}`
> - 3️⃣ you can define an "empty" DynamicObject and add the fields later
> - 4️⃣ it will print `DynamicObject{}`
> - 5️⃣ add setters and values dynamically
> - 6️⃣ it will print `DynamicObject{firstName=John, lastName=Doe}`
> - 7️⃣ use of getters; and it will print `Hello I'm Jane Doe`

✋ **To run it**, type this command: `golo golo --files 01-dynamicobjects-fields.golo`

## Add methods to a DynamicObject

Adding a method to a DynamicObject is like to add a closure to a field.

> **02-dynamicobjects-methods.golo**
```golo
module hello.dynamic.objects

function main = |args| {

  let jane = DynamicObject()
    : firstName("Jane")
    : lastName("Doe")
    : hello(|this| { # 1️⃣
        println("Hello I'm " + this: firstName() + " " + this: lastName())
    })
    : message(|this, message| { # 2️⃣
        println(
          "I'm " +
          this: firstName() + " " +
          this: lastName() + ": " +
          message
        )
    })

  jane: hello() # 3️⃣
  jane: message("👋 hello world 🌍") # 4️⃣

  jane: about(|this| -> "I'm " + this: firstName()) # 5️⃣
  println(jane: about()) # 6️⃣
}

```

> - 1️⃣ the name pf the method is the name of the field; and the value is a closure with a self reference to the object as the first parameter
> - 2️⃣ a DynamicObject method can have several parameters (with always the first parameter as a reference to the current object)
> - 3️⃣ it will print `Hello I'm Jane Doe`
> - 4️⃣ it will print `I'm Jane Doe: 👋 hello world 🌍`
> - 5️⃣ you can add a method dynamically when/where you want
> - 6️⃣ it will print `I'm Jane`

✋ **To run it**, type this command: `golo golo --files 02-dynamicobjects-methods.golo`

## Use a function as a constructor

If you need a constructor for you DynamicObject, it's easy! Use a function returning a DynamicObject:

> **03-dynamicobjects-constructors.golo**
```golo
module hello.dynamic.objects

function Human = |firstName, lastName| { # 1️⃣
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

  let jane = Human("Jane", "Doe") # 2️⃣

  jane: hello() # 3️⃣
  jane: message("👋 hello world 🌍") # 4️⃣

}
```

> - 1️⃣ define a function returning a DynamicObject
> - 2️⃣ call the function
> - 3️⃣ it will print `Hello I'm Jane Doe`
> - 4️⃣ it will print `I'm Jane Doe: 👋 hello world 🌍`

✋ **To run it**, type this command: `golo golo --files 03-dynamicobjects-constructors.golo`

ℹ️ more about dynamic objects: https://golo-lang.org/documentation/next/#_dynamic_objects

**This is the end of the "DynamicObjects' tutorial"**. You can retrieve the source code here: [resources/dynamicobjects](resources/dynamicobjects).
