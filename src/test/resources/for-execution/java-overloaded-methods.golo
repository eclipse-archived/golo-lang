module golotest.execution.JavaOverloadedMethods

function callFoo = |receiver, value| {
  return receiver: foo(value)
}

function passInt = |obj| -> callFoo(obj, 69)
function passString = |obj| -> callFoo(obj, "Yo!")