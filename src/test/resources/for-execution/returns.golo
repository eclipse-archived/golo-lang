module golotest.execution.FunctionsWithReturns

function empty = { }

function direct_return = { return }

local function ignore_me = { }

function return_42 = { return 42 }

function return_hello_world = { return "Hello, world!" }

function yes = { return ((true)) }

function no = { return (false) }

function escaped_string = {
  return "\nFoo\r\n\\n"
}

function escaped_char = {
  return '\n'
}

function multiline = {
  return """This is
*awesome*"""
}

function nasty_multiline = {
  return """Damn!=\"""="""
}

function raw_code = {
  return """println("Hello!\n")"""
}

function main = |args| {}

function issue509 = {
  return # comment 1
  # comment 2
  42
}