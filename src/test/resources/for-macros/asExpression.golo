
module golo.test.AsExpressionTest

import org.testng.Assert

&use("golo.test.AsExpressionMacros")

# function foo = -> 42
#
# function add = |a, b|-> a + b
#
# function test = {
#   let result = list[]
#   result: add(&answer())
#   result: add(&answer2())
#   result: add(add(&answer(), &answer2()))
#   result: add(&answer() + &answer2())
#
#   foreach (i in [40..&answer()]) {
#     result: add(i)
#   }
#   result: add([&answer(), &answer2()])
#
#   assertEquals(result, list[
#     42,
#     42,
#     84,
#     84,
#     40,
#     41,
#     [42,42]
#   ])
# }
#
# function main = |args| {
#   test()
#   println("ok")
# }
