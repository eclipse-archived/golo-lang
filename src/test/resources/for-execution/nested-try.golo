module golotest.execution.NestedTry

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test = {
  var r = list[]
  try {
    r: add("t")
  } finally {
    try {
      r: add("f")
      raise("err")
    } catch (e) {
      e: add("c")
    }
  }
  assertThat(r, contains("t", "f", "c"))
}

