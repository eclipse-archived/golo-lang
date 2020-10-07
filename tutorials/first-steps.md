# First steps with Golo

> **It will be a nice journey, you'll learn:**
> - hello world
> - variables, constants
> - functions
> - closures
> - control flow
> - exceptions
> - Java types
> - modules

This tutorial will allow beginners to get comfortable quickly. The main goal of this tutorial is to learn by example. You will be able to deduce certain things only thanks to the code examples (so I will not explain them) but do not hesitate to refer to the [documentation](https://golo-lang.org/documentation/next/index.html).

For this tutorial, we'll use Golo as a scripting language (but it is possible to compile Golo programs too).

## The essential "Hello World!"

Use your favorite code editor ([Micro](https://micro-editor.github.io/) is nice, runs in the terminal and Golo support is native), and create a new `01-hello.golo` file:

> **01-hello.golo**
```golo
module hello.world # 1️⃣

function main = |args| { # 2️⃣
  println("Hello world!")
}
```

> - 1️⃣ always start with a module name
> - 2️⃣ a Golo program has alaways a `main` function, and you use the pipe notation to declare the parameters

✋ **To run it**, type this command: `golo golo --files 01-hello.golo`

You'll get this output:

```shell
Hello world!
```

## Variables, constants and data literals

Variables and constants need to be initialized when declared.

Create a new `02-variables-and-co.golo` file:

> **02-variables-and-co.golo**
```golo
module hello.world

function main = |args| {
  # --- Variables versus Constants ---

  var greetings = "" # 1️⃣

  let firstName = "Bob" # 2️⃣ this is a constant references
  let lastName ="Morane"

  greetings = "Hello " + firstName + " " + lastName

  println(greetings)

  # --- Data Literals --- 3️⃣

  let this_is_null = null
  let this_is_true = true # 4️⃣

  let this_is_a_string = "good morning" # 5️⃣
  let this_is_a_character = 'G' # 6️⃣

  let this_is_an_integer = 42 # 7️⃣
  let this_is_a_double = 4.2 # 8️⃣

  let sum_is_a_double = this_is_an_integer + this_is_a_double # 9️⃣ java.lang.Double

  println(sum_is_a_double) # 1️⃣0️⃣

  # I love emojis 1️⃣1️⃣
  let 😡 = "bad"
  let 😶 = "oh oh"

  println(😶 + ", it's " + 😡) # 1️⃣2️⃣

}
```

> - 1️⃣ this is a variable
> - 2️⃣ this is a constant references
> - 3️⃣ Golo supports a set of data literals. They directly map to their counterparts from the Java Standard API. For the complete set, see [https://golo-lang.org/documentation/next/index.html#_data_literals](https://golo-lang.org/documentation/next/index.html#_data_literals).
> - 4️⃣ `java.lang.Boolean`
> - 5️⃣ `java.lang.String`
> - 7️⃣ `java.lang.Integer`
> - 8️⃣ `java.lang.Double`
> - 9️⃣ `java.lang.Double`
> - 1️⃣0️⃣ you'll get `46.2`
> - 1️⃣1️⃣ Golo identifiers can be non-ascii characters (e.g., Japanese, Emoji, Arabic, etc)
> - 1️⃣2️⃣ you'll get `oh oh, it's bad`

✋ **To run it**, type this command: `golo golo --files 02-variables-and-co.golo`

## Functions

Functions are first-class citizen in Golo. And there are various way to define these functions.

Create a new `03-functions.golo` file:

> **03-functions.golo**
```golo
module hello.world

----
This function takes no argument
and return a string 0️⃣
----
function hello = { # 1️⃣
  return "Hello world!"
}

----
This function takes one argument
and return a string
----
function hello_who = |name| { # 2️⃣
  return "Hello " + name
}

----
This function takes two arguments
and return a string
----
function hello_people = |name_1, name_2| -> "Hello " + name_1 + " and " + name_2 # 3️⃣

----
This is my main function
----
function main = |args| { # 4️⃣
  println(hello())
  println(hello_who("Bob"))
  println(hello_people("Bob", "Bill"))
}

```

> - 0️⃣ you can use multi-lines comments at the header of the functions
> - 1️⃣ this is a parameter-less function
> - 2️⃣ you need to use the pipe notation to declare the parameter(s)
> - 3️⃣ you can use a compact form (`|arg| -> 42`) to define a regular function (you don't need to use the return keyword)
> - 4️⃣ you'll get:
>    ```
>    Hello world!
>    Hello Bob
>    Hello Bob and Bill
>    ```

✋ **To run it**, type this command: `golo golo --files 03-functions.golo`

ℹ️ more about functions: [https://golo-lang.org/documentation/next/index.html#_functions](https://golo-lang.org/documentation/next/index.html#_functions).

## Closures

**From the Golo documentation**: *"Golo supports closures, which means that functions can be treated as first-class citizen. Defining a closure is straightforward as it derives from the way a function can be defined."*

Create a new `04-closures.golo` file:

> **04-closures.golo**
```golo
module hello.world

function main = |args| {

  let say_hello = -> println("Hello") # I know it's not pure 😝

  say_hello() # 1️⃣

  let say_hello_to_somebody = |name| {
    println("Hello " + name)
  }

  say_hello_to_somebody("Bob") # 2️⃣

  let sum = |a, b| -> a + b 3️⃣

  println(
    sum(10, sum(30, 2))
  ) # 4️⃣ it will print 42

}
```

> - 1️⃣ it will print `Hello`
> - 2️⃣ it will print `Hello Bob`
> - 3️⃣ Golo supports a compact form of closures for the cases where their body consists of a single expression
> - 4️⃣ it will print `42`

✋ **To run it**, type this command: `golo golo --files 04-closures.golo`

ℹ️ more about closures: [https://golo-lang.org/documentation/next/index.html#_closures](https://golo-lang.org/documentation/next/index.html#_closures).


> Most of the time, I say **Lambdas** instead **Closures**. Is this a mistake? [Are Java 8 Lambdas Closures?](https://www.bruceeckel.com/2015/10/17/are-java-8-lambdas-closures/).

## Control flow

Control flow in Golo has usual constructions (and some less ordinary, but we'll see it later).

Create a new `05-control-flow.golo` file:

> **05-control-flow.golo**
```golo
module hello.world

function main = |args| {

  # === if then else ===
  let value = 5

  if value <= 5 { # 1️⃣
    println("value is <= 5")
  } else {
    println("value is > 5")
  }

  # === case when ===
  # the case construction is used inside a closure or a function
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

  println (choice("two")) # 2️⃣
  println (choice("three")) # 3️⃣

  # you can use the case as a statement
  let another_value = "one"
  case {
      when another_value == "one" {
          println("ONE")
      }
      when another_value == "two" {
          println("TWO")
      }
      otherwise {
          println("NOT IN THE LIST")
      }
  } # 4️⃣

  # === match when then ===
  let your_choice = "one"

  let and_the_result_is = -> match {
    when your_choice == "one" then "ONE"
    when your_choice == "two" then "TWO"
    otherwise "NOT IN THE LIST"
  }
  # The values to be returned are specified after a then keyword that follows a boolean expression to be evaluated.

  println(and_the_result_is()) # 5️⃣

  # === Loops ===

  # while
  var counter = 0
  while (counter < 10) {
    counter = counter + 1
  }
  println(counter) # 6️⃣

  # for
  counter = 0
  for (var i = 0, i <= 10, i = i + 1) {
    counter = i
  }
  println(counter) # 7️⃣

}
```

> - 1️⃣ it will print `value is <= 5`
> - 2️⃣ it will print `TWO`
> - 3️⃣ it will print `NOT IN THE LIST`
> - 4️⃣ it will print `ONE`
> - 5️⃣ it will print `ONE`
> - 6️⃣ it will print `10`
> - 7️⃣ it will print `10`

✋ **To run it**, type this command: `golo golo --files 05-control-flow.golo`

ℹ️ more about control flow: [https://golo-lang.org/documentation/next/index.html#_control_flow](https://golo-lang.org/documentation/next/index.html#_control_flow).

## Exceptions

The semantics are the same as found in other languages such as Java.

Create a new `06-exceptions.golo` file:

> **06-exceptions.golo**
```golo
module hello.world

function main = |args| {

  try {
    println(10/0)
  } catch (exception) {
    println(exception) # 1️⃣
  } finally {
    println ("that's the end") 2️⃣
  }

}
```

> - 1️⃣ it will print `java.lang.ArithmeticException: / by zero`
> - 2️⃣ it will finally print `that's the end`

✋ **To run it**, type this command: `golo golo --files 06-exceptions.golo`

ℹ️ more about exceptions: [https://golo-lang.org/documentation/next/index.html#_exceptions](https://golo-lang.org/documentation/next/index.html#_exceptions)

## Golo and Java types

There will be a complete tutorial dedicated to the topic "Golo and Java", but it's essential to know how Golo handles the Java types like String, Integer, etc. ...

Golo types are Java types, so, you can call the usual methods of Java types.

Create a new `07-java-types.golo` file:

> **07-java-types.golo**
```golo
module hello.world

function main = |args| {

  let message = "Hello World"

  println(
    message: startsWith("Hello") # 1️⃣
  )

  println(
    message: contains("World") # 2️⃣
  )

  println(
    Integer.valueOf("42") # 3️⃣
  )

}
```

> - 1️⃣ & 2️⃣ use the `:` notation to call an instance method
> - 1️⃣ it will print `true`
> - 2️⃣ it will print `true`
> - 3️⃣ use the `.` notation to call a static method of a type
> - 3️⃣ it will print `42`

✋ **To run it**, type this command: `golo golo --files 07-java-types.golo`

## Modules

### Define a reusable module

Create a new `my-tools.golo` file:

> **my-tools.golo**
```golo
module mytools

function sayHello = {
  return "Hello"
}

function sayHello = |name| { # 1️⃣
  return "Hello " + name
}
```

> - 1️⃣ you can override functions

### Use our awesome module

Create a new `08-modules.golo` file:

> **08-modules.golo**
```golo
module hello.world

import mytools

function main = |args| {
  println(sayHello()) # 1️⃣
  println(sayHello("Bob")) # 2️⃣
}
```

> - 1️⃣ it will print `Hello`
> - 2️⃣ it will print `Hello Bob`

✋ **To run it**, type this command: `golo golo --files my-tools.golo 08-modules.golo`

> Always end the command with the main module

ℹ️ more about modules: [https://golo-lang.org/documentation/next/index.html#_functions_from_other_modules_and_imports](https://golo-lang.org/documentation/next/index.html#_functions_from_other_modules_and_imports)

**This is the end of this quick start**. You can retrieve the source code here: [resources/first-steps](resources/first-steps).
