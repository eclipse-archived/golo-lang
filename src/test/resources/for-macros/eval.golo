module golo.test.EvalTest

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

&eval {
  let fn = map[
    ["answer", 42],
    ["foo", "bar"],
    ["hello", "world"]
  ]
  foreach name, value in fn: entrySet() {
    self: enclosingModule(): add(`function(name): returns(constant(value)))
  }
}

&eval {
  return `function("returned"): returns(constant("result"))
}

function test_sideeffect = {
  assertThat(answer(), `is(42))
  assertThat(foo(), `is("bar"))
  assertThat(hello(), `is("world"))
}

function test_returned = {
  assertThat(returned(), `is("result"))
}

function main = |args| {
  test_sideeffect()
  test_returned()
}
