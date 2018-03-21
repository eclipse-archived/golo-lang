----
no import, call FQN
----
module Test33
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_ = {
  assertThat(plop.Foo.polymorphic(), `is("Foo::polymorphic"))
  assertThat(plop.Bar.polymorphic(42), `is("Bar::polymorphic"))
}
