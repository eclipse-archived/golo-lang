module golotest.execution.DestructuringOld

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

import org.eclipse.golo.runtime

&useOldstyleDestruct()

function test_tuple_old = {
  let fst, scd = [1, 2, 3, 4]
  assertThat(fst, `is(1))
  assertThat(scd, `is(2))
}

function test_list_old = {
  let a, b... = list[1, 2, 3, 4]
  assertThat(a, `is(1))
  assertThat(b, `is(tuple[2, 3, 4]))
}

function main = |args| {
  test_tuple_old()
  test_list_old()
}
