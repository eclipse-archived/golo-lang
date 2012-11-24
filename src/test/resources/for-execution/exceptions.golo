module golotest.execution.Exceptions

import java.lang.RuntimeException

function runtimeException = {
  throw RuntimeException("w00t", null)
}

function catch_exception = {
  try {
    runtimeException()
  } catch (e) {
    return "ok"
  }
  return "woops"
}
