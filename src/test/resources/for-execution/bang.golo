module golotest.execution.Bang

var decorator_parameter = 17

function set_decorator_parameter = |value| {
  decorator_parameter = value
}

function identity = |a| -> a

function adder = |a| -> |b| -> |c| -> a + b + c

function decorator = |func| {
  let marker = java.lang.Object()
  let wrapper = -> marker:hashCode()
  return wrapper
}

function parametrized_decorator = |param| -> |func| {
  let marker =java.lang.Object()
  let wrapper = -> marker:hashCode()
  return wrapper
}

function func_test = |a| -> identity!(a)

function null_test = |a| -> identity!(a)

function reference_test = |a| {
  let f = ^identity
  return f!(a)
}

function singleton = -> java.lang.Object!()

function anonymous = |a, b, c| -> adder!(a)!(b)!(c)

@!decorator
function decorated = -> 42

@!parametrized_decorator(decorator_parameter)
function parametrized_decorated =  -> 42

function test_parametrized_decorated = -> parametrized_decorated()

function test_decorated = -> decorated()