module GoloDispatch

function dispatchPlop = |plop| -> plop: plop()

struct Plop = { random }

augment GoloDispatch.types.Plop {

  function plop = |this| -> this: random(): nextInt()
}

function struct_plop = -> Plop(): random(java.util.Random())

function dynamic_plop = {
  let random = java.util.Random()
  let plop = DynamicObject()
  plop: define("plop", |this| -> random: nextInt())
  return plop
}

function dynamic_plop_with_internal_state = {
  let plop = DynamicObject()
  plop: define("_random", java.util.Random())
  plop: define("plop", |this| -> this: _random(): nextInt())
  return plop
}
