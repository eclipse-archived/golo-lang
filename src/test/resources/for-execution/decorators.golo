module golotest.execution.Decorators

function callFirst = |func| -> -> func() + "1"

function callSecond = |func| -> -> func() + "2"

@callSecond
@callFirst
function decorator_order = -> ""

function generic_decorator = |func| -> |args...| -> "(" + func: invoke(args)  + ")"

@generic_decorator
function generic_decorator_simple = |arg1,arg2| -> arg1 + arg2

@generic_decorator
function generic_decorator_parameterless =  -> "test"

@generic_decorator
function generic_decorator_varargs = |args...| {
  var acc = ""
  foreach arg in args {
    acc = acc + arg
  }
  return acc
}

function checkInput = |types...| -> |func| -> |args...| {
  for (var i = 0, i < args:length(), i = i + 1) {
    require(args: get(i) oftype types: get(i) , "arg"+i+" must be a "+types: get(i) )
  }
  return func: invokeWithArguments(args)
}

@checkInput(Integer.class)
function check_args = |x| -> x

function sayHello = |func| {
  return |str| -> "Hello "+str+"!"
}

augment java.lang.String {
  @sayHello
  function greet = |this| -> this
}

function scale = |factor| -> |func| -> |this, args...| -> func(this, args: get(0) * factor, args: get(1) * factor)

function scaleOne = |factor| -> |func| -> |this, a| -> func(this, a * factor)

struct Point = {x, y}

augment golotest.execution.Decorators.types.Point {

    @scale(10)
    function translate = |this, x, y| {
        this: x(x)
        this: y(y)
    }
}

augmentation Translations = {

    @scaleOne(10)
    function translateX = |this, x| {
        this: x(x)
    }

    @scaleOne(10)
    function translateY = |this, y| {
        this: y(y)
    }
}

augment golotest.execution.Decorators.types.Point with Translations

function curry = |f| -> |a| -> |b| -> f(a, b)

@curry
function curryfied = |a,b| -> a + b

function test_decorator_order = -> decorator_order()

function test_generic_decorator_simple = -> generic_decorator_simple(12,30)

function test_generic_decorator_parameterless = -> generic_decorator_parameterless()

function test_generic_decorator_varargs0 = -> generic_decorator_varargs()

function test_generic_decorator_varargs1 = -> generic_decorator_varargs(4)

function test_generic_decorator_varargs2 = -> generic_decorator_varargs(4,2)

function test_generic_decorator_varargs2_from_reference = {
    let func = ^generic_decorator_varargs
    return func(4,2)
}

function test_generic_decorator_varargs2_from_enclosed_reference = {
    let func = ^generic_decorator_varargs
    let closure = -> func(4,2)
    return closure()
}

function test_check_args = -> check_args("42")

function test_decorated_augmentation = -> "Golo Decorator":greet()

function test_struct_decorated_augmentation = {
    let point = Point(0,0)
    point: translate(4,2)
    return point: toString()
}

function test_struct_decorated_named_augmentation = {
    let point = Point(0,0)
    point: translateX(4)
    point: translateY(2)
    return point: toString()
}

function test_curryfied_function = -> curryfied(12)(30)

@(|f| -> -> "simple" + f())
local function plop_simple = -> "plop"

function test_expr_decorator_simple = -> plop_simple()

@(|msg| -> |f| -> |args| -> msg + f: invoke(args))("pre")
local function plop = |m| -> "plop" + m

function test_expr_decorator = -> plop("daplop")

