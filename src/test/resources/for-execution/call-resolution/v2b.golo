
module test2b

import plop.Foo
import plop.Bar

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(Foo.doit(), `is("Foo::doit"))
  assertThat(Bar.doit(), `is("Bar::doit"))
}
