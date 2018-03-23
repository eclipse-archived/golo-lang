----
import both modules, call FQN
----
module Test312

import plop.Foo
import plop.Bar
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(plop.Foo.polymorphic(), `is("Foo::polymorphic"))
  assertThat(plop.Bar.polymorphic(42), `is("Bar::polymorphic"))
}
