module golotest.execution.JavaOverloadedMethods

function callFoo = |receiver, value| {
  return receiver: foo(value)
}

function passInt = |obj| -> callFoo(obj, 69)
function passString = |obj| -> callFoo(obj, "Yo!")

function callBar = |receiver, a, b| {
  return receiver: bar(a, b)
}

function barStringInt = |obj| -> callBar(obj, "Plop", 69)
function barIntLong = |obj| -> callBar(obj, 69, 100_L)

function callBaz = |receiver, a, b, c, d, e, f, g, h| {
  return receiver: baz(a, b, c, d, e, f, g, h)
}

function bazAllString = |obj| -> callBaz(obj, "a", "b", "c", "d", "e", "f", "g", "h")
function bazMixed = |obj| -> callBaz(obj, "a", "b", "c", "d", "e", "f", 1, 2)
