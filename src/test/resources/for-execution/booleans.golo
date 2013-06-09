module golotest.execution.Booleans

function and_logic = {
  if (true and true) != true {
    raise("woops")
  }
  if (false and true) != false {
    raise("woops")
  }
  if (true and false) != false {
    raise("woops")
  }
  if (false and false) != false {
    raise("woops")
  }
}

function or_logic = {
  if (true or true) != true {
    raise("woops")
  }
  if (false or true) != true {
    raise("woops")
  }
  if (true or false) != true {
    raise("woops")
  }
  if (false or false) != false {
    raise("woops")
  }
}

function and_shortcut = {
  let foo = null
  if (foo isnt null) and (foo: bogus() == 666) {
    return "WTF"
  } else {
    return "Ok"
  }
}

function or_shortcut = {
  let foo = null
  if (true) or (foo: plop()) {
    return "Ok"
  } else {
    return "WTF"
  }
}
