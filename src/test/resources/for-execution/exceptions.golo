module golotest.execution.Exceptions

import java.lang.RuntimeException

function runtimeException = {
  throw RuntimeException("w00t", null)
}
