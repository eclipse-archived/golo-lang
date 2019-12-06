module golo.test.ResolveFQN

import org.testng.Assert

&use("golo.test.AsExpressionMacros")
&use("golo.test.ResolveFQNMacros")



function test = {
  assertEquals(&golo.test.AsExpressionMacros.answer(), 42)
  assertEquals(&golo.test.ResolveFQNMacros.answer2(), 1337)
}
