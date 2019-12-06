
module golo.test.SimpleTest

import org.testng.Assert

&use("golo.test.SimpleMacros")

&create("foobar")

function test = {
  let hello = list[]
  foreach name in ["world", "Ford", "everybody"] {
    &sayHello(hello, name)
  }
  &sayHello(hello, "da plop")

  let f = &createLambda("hello")
  let g = &createLambda("bye")

  assertEquals(hello, list["Hello world", "Hello Ford", "Hello everybody", "Hello da plop"])
  assertEquals(foobar(), "foobar in golo.test.SimpleTest")
  assertEquals(f(), "hello in test")
  assertEquals(g(), "bye in test")
}

function test_swap = {
#tag::test_swap[]
  let tmp = "foo"
  var first = 42
  var second = 69
  swap(first, second)
  first = first + 1
  swap(first, second)
#end::test_swap[]

  assertEquals(first, 42)
  assertEquals(second, 70)
  assertEquals(tmp, "foo")
}

function test_answer = {
  assertEquals(golo.test.SimpleMacros.answer(): class(),
               java.lang.Integer.class)
  assertEquals((^golo.test.SimpleMacros::answer()): class(),
               gololang.ir.ConstantStatement.class)
}

function test_dowhile = {
#tag::dowhile[]
  let l = list[]
  var i = 0
  &doWhile(i > 0) {
    l: add(i)
    i = i - 1
  }
#end::dowhile[]

  assertEquals(l, list[0])

  l: clear()
  i = 2
  &doWhile(i > 0) {
    l: add(i)
    i = i - 1
  }
  assertEquals(l, list[2, 1])
}

function main = |args| {
  test()
  test_swap()
  test_answer()
  test_dowhile()
  println("ok")
}

