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

function dans_la_case_il_y_a_de_l_espace = | obj | {
  case {

    when obj oftype String.class {
      return "String"
    }
    #Comment or blank lines can be inserted
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

function dans_ton_match_il_y_a_de_l_espace = |obj| -> match {

  when obj oftype String.class then "String"
  when obj oftype Integer.class then "Integer"
  #Comment or blank lines can be inserted
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

function agrigolo = {
  let add_animal = |player, animal, number| -> match {
    when
      animal == "horse"
      then
        println(player + " has " + number + " horses")
    when animal == "sheep"
      then println(player + " has " + number + " sheeps")
    otherwise
      println("herr, not an animal buddy")
  }
  println("Agrigolo")
  add_animal("player2", "horse", 3)
  let add_animal_to_player1 = add_animal: bindTo("player1")
  add_animal_to_player1("sheep", 5)
  let add_horses_to_player1 = add_animal_to_player1: bindTo("horse")
  add_horses_to_player1(6)
}

