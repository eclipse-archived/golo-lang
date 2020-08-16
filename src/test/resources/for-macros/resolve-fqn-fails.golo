module golo.test.ResolveFQNFails

import org.testng.Assert

&use("golo.test.AsExpressionMacros")
&use("golo.test.ResolveFQNMacros")



function test = {
  &golo.test.ResolveFQNMacros.answer()
}

