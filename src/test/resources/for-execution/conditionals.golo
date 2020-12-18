module golotest.execution.Conditionals

import java.lang

function simple_if = {
  if ((true)) {
    return "ok"
  }
}

function simple_if_else = {
  if false {
    return null
  } else {
    return "ok"
  }
}

function simple_if_elseif_else = {
  if (false) {
    return null
  } else if false {
    return null
  } else {
    return "ok"
  }
}

function boolean_to_string = | boolean | {
  if boolean {
    return "true"
  } else {
    return "false"
  }
}

#tag::what[]
function what = |obj| {
  case {
    when obj oftype String.class {
      return "String"
    }
    when obj oftype Integer.class {
      return "Integer"
    }
    otherwise {
      return "alien"
    }
  }
}
#end::what[]

function if_else_var = {
  var foo = null
  if true {
    foo = "true"
  } else {
    foo = "false"
  }
  return foo
}

function what_match = |obj| -> match {
  when obj oftype String.class then "String"
  when obj oftype Integer.class then "Integer"
  otherwise "alien"
}

function match_email = {
#tag::match_email[]
  let item = "foo@bar.com"

  let what_it_could_be = |it| -> match {
    when it: contains("@") then "an email?"
    when it: startsWith("+33") then "a French phone number?"
    when it: startsWith("http://") then "a website URL?"
    otherwise "I have no clue, mate!"
  }

  require(what_it_could_be(item) == "an email?", "error")
#end::match_email[]
}
