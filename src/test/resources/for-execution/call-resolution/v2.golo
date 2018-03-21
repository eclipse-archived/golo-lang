
module test2a

import plop.Foo
import plop.Bar

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(doit(), `is("Foo::doit"))
}
