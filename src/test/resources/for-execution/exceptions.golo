module golotest.execution.Exceptions

import java.lang.RuntimeException

function runtimeException = {
  throw RuntimeException("w00t")
}

function catch_exception = {
  try {
    runtimeException()
  } catch (e) {
    return "ok"
  }
  return "woops"
}

function finally_no_exception = {
  var a = null
  try {
    a = "ok"
  } catch (e) { return "woops" } finally { return a }
}

function finally_with_exception = {
  try {
    runtimeException()
  } catch (e) {
    throw e
  } finally {
    throw RuntimeException("ok", null)
  }
}

function try_finally = {
  try {
    runtimeException()
  } finally {
    throw RuntimeException("ok", null)
  }
}

function raising = {
  raise("Hello")
}

function nested_try = {
  try {
    try {
      raise("a")
    } catch (ok) {
      return "ok"
    }
  } catch (failed) {
    return "failed"
  }
}
