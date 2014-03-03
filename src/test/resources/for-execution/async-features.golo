module golotest.execution.Asyncfeatures

import gololang.Async

local function except = -> java.lang.RuntimeException("Plop")

function check_map = {
  let ok = setFuture("Ok"): map(|v| -> v + "!"): get()
  let failed = failedFuture(except()): map(|v| -> v): get()
  return [ok, failed]
}

function check_flatMap = {
  let ok = setFuture("Ok"): flatMap(|v| -> setFuture(v + "!")): get()
  let failed = failedFuture(except()): flatMap(|v| -> setFuture(v)): get()
  return [ok, failed]
}

function check_filter = {
  let pred = |s| -> s: startsWith("Ok")
  let ok = setFuture("Ok"): filter(pred): get()
  let bam = setFuture("Foo"): filter(pred): get()
  let failed = failedFuture(except()): filter(pred): get()
  return [ok, bam, failed]
}

function check_fallbackTo = {
  let ok = setFuture("Ok"): fallbackTo(setFuture("Bam")): get()
  let failover = failedFuture(except()): fallbackTo(setFuture("Yeah")): get()
  let bigfail = failedFuture(except()): fallbackTo(failedFuture(java.lang.AssertionError())): get()
  return [ok, failover, bigfail]
}

function check_all = ->
  all([setFuture("foo"), setFuture("bar"), failedFuture(except())]): get()

function check_any = ->
  any([failedFuture(except()), failedFuture(except()), setFuture("ok"), failedFuture(except())]): get()

function check_any_none = ->
  any([failedFuture(except()), failedFuture(except()), failedFuture(except())]): get()

function check_reduce = {
  let f1 = [setFuture("a"), setFuture("b"), setFuture("c")]
  let f2 = [setFuture("a"), failedFuture(except()), setFuture("c")]
  let reducer = |acc, next| -> acc + next
  return [
    reduce(f1, "", reducer): get(),
    reduce(f2, "", reducer): get()
  ]
}
