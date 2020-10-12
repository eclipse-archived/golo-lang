module golotest.execution.NestedTry

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers


function in_finally = |r, raise_in_try, raise_in_finally| {
  try {
    r: add("t")
    if raise_in_try {
      raise("et")
    }
  } finally {
    try {
      r: add("f")
      if raise_in_finally {
        raise("ef")
      }
    } catch (e) {
      r: add(e: getMessage())
    }
  }
}


function test_in_finally = {
  let r = list[]
  in_finally(r, false, false)
  assertThat(r, contains("t", "f"))

  r: clear()
  in_finally(r, false, true)
  assertThat(r, contains("t", "f", "ef"))

  r: clear()
  try {
    in_finally(r, true, false)
  } catch(e) {
    assertThat(e: message(), `is("et"))
    assertThat(r, contains("t", "f"))
  }
  r: clear()
  try {
    in_finally(r, true, true)
  } catch(e) {
    assertThat(e: message(), `is("et"))
    assertThat(r, contains("t", "f", "ef"))
  }
}

function in_finally_with_catch = |raise_in_try, raise_in_finally| {
  var r = list[]
  try {
    r: add("t")
    if raise_in_try {
      raise("et")
    }
  } catch(e) {
    r: add(e: getMessage())
  } finally {
    try {
      r: add("f")
      if raise_in_finally {
        raise("ef")
      }
    } catch (e) {
      r: add(e: getMessage())
    }
  }
  return r
}

function test_in_finally_with_catch = {
  assertThat(in_finally_with_catch(true, true), contains("t", "et", "f", "ef"))
  assertThat(in_finally_with_catch(false, false), contains("t", "f"))
  assertThat(in_finally_with_catch(true, false), contains("t", "et", "f"))
  assertThat(in_finally_with_catch(false, true), contains("t", "f", "ef"))
}

