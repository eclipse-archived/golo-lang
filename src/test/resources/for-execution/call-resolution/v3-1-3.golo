
----
import both modules, call with only function name
----
module Test313

import plop.Foo
import plop.Bar
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(polymorphic(), `is("Foo::polymorphic"))
  assertThat(polymorphic(42), `is("Bar::polymorphic"))
}
