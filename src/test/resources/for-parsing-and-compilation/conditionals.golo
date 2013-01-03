module Conditionals

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

function dans_la_case = | obj | {
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

function dans_ton_match = |obj| -> match {
  when obj oftype String.class then "String"
  when obj oftype Integer.class then "Integer"
  otherwise "alien"
}

function if_else_var = {
  var foo = null
  if true {
    foo = "true"
  } else {
    foo = "false"
  }
  return foo
}
