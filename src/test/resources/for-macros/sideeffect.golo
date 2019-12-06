module golo.test.SideeffectTest

import org.testng.Assert

&use("golo.test.SideeffectMacros")

function test = {
#tag::test_sideeffect[]
  let v = foo() + &sideeffect(1)
#end::test_sideeffect[]
  assertEquals(v, 42)

  assertEquals(bar() + sideeffect(1), 42)
}

function main = |args| {
  test()
  println("ok")
}
