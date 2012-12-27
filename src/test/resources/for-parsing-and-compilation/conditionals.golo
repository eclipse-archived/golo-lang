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
