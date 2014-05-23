module golotest.execution.ModuleState

let prefix = ">>> "

var counter = 0

let foo = -> "Foo!"

augment java.lang.Object {

  function raise_it_for_fun = |this| {
    counter = counter + 10
    return prefix + counter
  }
}

function riseUp = {
  counter = counter + 1
  return counter
}

function display = -> prefix + counter

function for_fun = {
  return 0: raise_it_for_fun()
}

function give_foo = -> foo()

