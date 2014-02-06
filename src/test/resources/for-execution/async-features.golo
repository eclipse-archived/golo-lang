module golotest.execution.Asyncfeatures

import gololang.Async

function check_map = {
  let ok = setFuture("Ok"): map(|v| -> v + "!"): get()
  let failed = failedFuture(java.lang.RuntimeException("Plop")): map(|v| -> v): get()
  return [ok, failed]
}

function check_flatMap = {
  let ok = setFuture("Ok"): flatMap(|v| -> setFuture(v + "!")): get()
  let failed = failedFuture(java.lang.RuntimeException("Plop")): flatMap(|v| -> setFuture(v)): get()
  return [ok, failed]
}
