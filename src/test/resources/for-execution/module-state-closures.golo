
module golotest.execution.ModuleStateClosures

# Utility functions
local function identity = |x| -> x
local function addCurry = |x| -> |y| -> x + y

# One can define a constant value as a module state
let CONSTANT_VALUE = 42

# One can assign an expression to a module state
let CONSTANT_EXPR = 21 + 21

# One can set a module state from a function result
let CONSTANT_CALL_CONSTANT = identity(42)

# Module state can be used in a regular function
local function foo = |x| -> x + CONSTANT_VALUE

# Module state can  be used in a closure (curry)
local function bar = |x| -> |y| -> x + y + CONSTANT_VALUE

# Module state can be a function ref
let CONSTANT_FUNREF = ^foo
let CONSTANT_REF_CURRY = ^addCurry
let CALL_FUNREF = identity(^foo)

# Module state can be closure on other module state
let CLOSED_FUNREF = addCurry(CONSTANT_VALUE)

# Module state can be a lambda
let CONSTANT_CONSTANT_LAMBDA = -> 42
let CALL_CONSTANT_LAMBDA = identity(-> 42)
let CONSTANT_LAMBDA = |x| -> 2 * x
let CALL_LAMBDA = identity(|x| -> 2 * x)

# Module state can be curryfied function
let CONSTANT_CURRY = |x| -> |y| -> x + y
let CALL_CURRY = identity(|x| -> |y| -> x + y)

# Module state can be a closure
let CONSTANT_CONSTANT_CLOSURE = -> CONSTANT_VALUE
let CALL_CONSTANT_CLOSURE = identity(-> CONSTANT_VALUE)
let CONSTANT_CLOSURE = |x| -> x + CONSTANT_VALUE
let CALL_CLOSURE = identity(|x| -> x + CONSTANT_VALUE)

function test = {
  # just check utility functions
  require(identity(42) == 42, "err")
  require(addCurry(21)(21) == 42, "err")
  let plus21 = addCurry(21)
  require(plus21(21) == 42, "err")

  # A constant module state can be accessed
  require(CONSTANT_VALUE == 42, "err")
  require(CONSTANT_EXPR == 42, "err")
  require(CONSTANT_CALL_CONSTANT == 42, "err")

  # A constant module state can be used in functions
  require(foo(1) == 43, "err")

  # A constant module state can be used as closed variable locally
  require(addCurry(CONSTANT_VALUE)(1) == 43, "err")
  require(addCurry(1)(CONSTANT_VALUE) == 43, "err")

  # A constant module state can be used in static closures
  let f = |x| -> x + CONSTANT_VALUE
  require(f(1) == 43, "err")

  # A constant module state can be used in generated closures (curry)
  let g = bar(1)
  require(g(-1) == 42, "err")
  require(bar(2)(-2) == 42, "err")

  # A module state containing a function ref can be called
  require(CONSTANT_FUNREF(1) == 43, "err")
  require(CONSTANT_REF_CURRY(21)(21) == 42, "err")
  require(CALL_FUNREF(1) == 43, "err")
  require(CLOSED_FUNREF(1) == 43, "err")

  # A module state containing a lambda can be called
  require(CONSTANT_CONSTANT_LAMBDA() == 42, "err")
  require(CALL_CONSTANT_LAMBDA() == 42, "err")
  require(CONSTANT_LAMBDA(21) == 42, "err")
  require(CALL_LAMBDA(21) == 42, "err")

  # A module state containing a curryfied function can be called
  require(CONSTANT_CURRY(21)(21) == 42, "err")
  require(CALL_CURRY(21)(21) == 42, "err")

  # A module state containing a closure can be called
  require(CONSTANT_CONSTANT_CLOSURE() == 42, "err")
  require(CALL_CONSTANT_CLOSURE() == 42, "err")
  require(CONSTANT_CLOSURE(1) == 43, "err")
  require(CALL_CLOSURE(1) == 43, "err")
}


function main = |args| {
  test()
  println("ok")
}
