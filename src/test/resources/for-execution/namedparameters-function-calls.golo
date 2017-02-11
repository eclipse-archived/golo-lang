module named_parameters

function create_post = |author, title, content| -> author + " " + title + " " + content

function create_post_ok = -> create_post(content = "Lorem Ipsum", title = "Awesome Post", author = "John")

function create_post_with_invalid_argument_name = -> create_post(foo = "Lorem Ipsum", title = "Awesome Post", author = "John")

function joiner = |delimiter, values...| {
  if (values: length() is 0) {
    return ""
  }
  let joined = StringBuilder(values: get(0))
  for (var i = 1, i < values: length(), i = i + 1) {
      joined: append(delimiter)
      joined: append(values: get(i))
  }
  return joined: toString()
}

function csv_builder = -> joiner(values = array["a", "b", "c"], delimiter = ",")

augment java.lang.String {
 function decorate = |self, prefix, suffix| -> prefix + self + suffix
 function append = |this, values...| -> joiner("", values)
}

function golo_decoratored = -> "Golo": decorate(suffix = ">", prefix = "<")

function golo_augmentation_varargs = -> "": append(values = array["a", "b", "c"])

local function foo = -> "Foo"
local function bar = -> null
local function plop = -> "Plop!"

function call_with_expressions = ->
  create_post(
    content = ">>> " + plop(),
    title = foo(),
    author = bar() orIfNull "Unknown"
  )
