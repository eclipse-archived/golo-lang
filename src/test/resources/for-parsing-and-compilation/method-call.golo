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
  bar
    : plop (
      foo : yeah()
    )
  foo: foo(): bar(): baz(bar: baz())
}

function c = {
  return "Hello": toString()
}

function d = {
  return java.lang.StringBuilder("h"):
    append("e"):
    append("l"):
    append("l"):
    append("o"):
    toString()
}

function escaped = {
  return Foo(): `is(): `not(): bad()
}

function elvis = -> null ?: foo() ?: bar() : baz()

function functional = |foo| -> foo: f(1, 2)(3)(): g()
