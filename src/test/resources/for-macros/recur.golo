
module golo.test.ReccurTest

import org.testng.Assert

&use("golo.test.ReccurMacros")

function test = {
  assertEquals(list[&sum(4), &sum(10)], list[10,55])
  assertEquals(list[&sum2(4), &sum2(10)], list[10,55])
}

function main = |args| {
  test()
  println("ok")
}
