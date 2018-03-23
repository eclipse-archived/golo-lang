
----
import only common package, call FQN
----
module Test322

import plop
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(plop.Foo.polymorphic(), `is("Foo::polymorphic"))
  assertThat(plop.Bar.polymorphic(42), `is("Bar::polymorphic"))
}
