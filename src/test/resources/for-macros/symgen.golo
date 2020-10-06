module golo.test.SideeffectTest

import org.testng.Assert


&use("gololang.ir.DSL")

&localSymbols()

function test = |args| {
  assertEquals(gensym(), "__$$_golo$test$SideeffectTest_1")
  enterSymScope("plop")
  assertEquals(gensym(), "__$$_golo$test$SideeffectTest_plop_2")
  exitSymScope()
  assertEquals(gensym(), "__$$_golo$test$SideeffectTest_3")
  assertEquals(gensym("bar"), "__$$_golo$test$SideeffectTest_bar_4")
}
