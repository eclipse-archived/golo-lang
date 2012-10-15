module method.call

function a = {
  let l = java.util.LinkedList()
  l
  l: add(1)
  l: add(2)
  l: add(3)
  return l
}

function b = {
  SomeFunc(1, 2, 3): foo("foo"): bar("bar"): baz("baz"):
                     awesome(): reallyIs(): cool()
  let foo = Foo()
  let bar = Bar()
  foo: foo(): bar(): baz(bar: baz())
}

function c = {
  return "Hello": toString()
}
