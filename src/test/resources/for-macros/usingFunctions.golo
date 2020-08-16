module golo.test.UsingFunctionsTest

import golo.test.UsingFunctionsMacros
import org.testng.Assert

function who = -> "Zaphod"

function test = {
  assertEquals(&printHello("World"), ":)")
  assertEquals(&printHello("Foo"), "Hello Foo")
  assertEquals(&answer(), 42)
  assertEquals(&answer2(), "Zaphod")
}

function main = |args| {
  test()
  println("ok")
}
