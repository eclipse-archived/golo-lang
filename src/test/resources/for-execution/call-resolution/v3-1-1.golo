----
Import both modules, call with only module name.
----
module Test311

import plop.Foo
import plop.Bar

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(Foo.polymorphic(), `is("Foo::polymorphic"))
  assertThat(Bar.polymorphic(42), `is("Bar::polymorphic"))
}
