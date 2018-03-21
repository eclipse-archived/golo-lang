----
import only common package, call with module
----
module Test321

import plop

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(Foo.polymorphic(), `is("Foo::polymorphic"))
  assertThat(Bar.polymorphic(42), `is("Bar::polymorphic"))
}

