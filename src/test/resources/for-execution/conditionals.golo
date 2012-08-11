module golotest.execution.Conditionals

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
