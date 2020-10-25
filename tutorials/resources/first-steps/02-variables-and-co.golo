module hello.world

function main = |args| {
  # --- Variables versus Constants ---

  var greetings = ""

  let firstName = "Bob"
  let lastName ="Morane"

  greetings = "Hello " + firstName + " " + lastName

  println(greetings)

  # --- Data Literals ---

  let this_is_null = null
  let this_is_true = true

  let this_is_a_string = "good morning"
  let this_is_a_character = 'G'

  let this_is_an_integer = 42
  let this_is_a_double = 4.2

  let sum_is_a_double = this_is_an_integer + this_is_a_double

  println(sum_is_a_double)

  # I love emojis
  let 😡 = "bad"
  let 😶 = "oh oh"

  println(😶 + ", it's " + 😡)

}


