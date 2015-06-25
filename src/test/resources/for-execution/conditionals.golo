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
