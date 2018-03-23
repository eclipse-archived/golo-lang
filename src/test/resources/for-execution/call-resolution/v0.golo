
module test0

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(plop.Foo.doit(), `is("Foo::doit"))
  assertThat(plop.Bar.doit(), `is("Bar::doit"))
}
