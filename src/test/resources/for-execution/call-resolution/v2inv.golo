
module test2ainv

import plop.Bar
import plop.Foo
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(doit(), `is("Bar::doit"))
}
function main = |args| {
  require(doit() == "Bar::doit", "err")
}
