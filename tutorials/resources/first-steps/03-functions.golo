module hello.world

----
This function takes no argument
and return a string
----
function hello = {
  return "Hello world!"
}

----
This function takes one argument
and return a string
----
function hello_who = |name| {
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
function main = |args| {
  println(hello())
  println(hello_who("Bob"))
  println(hello_people("Bob", "Bill"))
}

