module test4

import plop

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(delegate(), `is("Foo::doit"))
}

