
module test1

import plop

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(Foo.doit(), `is("Foo::doit"))
  assertThat(Bar.doit(), `is("Bar::doit"))
}
