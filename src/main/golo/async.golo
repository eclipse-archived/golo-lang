module gololang.Async

function promise = -> gololang.concurrent.async.Promise()

augment gololang.concurrent.async.Future {

  function map = |this, fun| {
    let p = promise()
    this: onSet(|v| -> p: set(fun(v))): onFail(|t| -> p: fail(t))
    return p: future()
  }

  function flatMap = |this, fun| {
    let p = promise()
    this: onSet(|v| ->
      fun(v): onSet(|r| -> p: set(r)): onFail(|t| -> p: fail(t))
    ): onFail(|t| -> p: fail(t))
    return p: future()
  }

  function filter = |this, pred| {
    let p = promise()
    this: onSet(|v| {
      if pred(v) {
        p: set(v)
      } else {
        p: fail(java.util.NoSuchElementException())
      }
    }): onFail(|t| -> p: fail(t))
    return p: future()
  }

  function fallbackTo = |this, future| {
    let p = promise()
    this: onSet(|v| -> p: set(v)): onFail(|t| {
      future: onSet(|v| -> p: set(v)): onFail(|e| -> p: fail(e))
    })
    return p: future()
  }
}

function all = |futures| {
  let size = futures: size()
  let vector = java.util.ArrayList(size)
  let p = promise()
  let i = java.util.concurrent.atomic.AtomicInteger(0)
  let trigger = {
    if i: incrementAndGet() == size {
      p: set(vector)
    }
  }
  for (var i = 0, i < size, i = i + 1) {
    futures: get(i):
      onSet(|v| {
        vector: add(i, v)
        trigger()
      }):
      onFail(|e| {
        vector: add(i, e)
        trigger()
      })
  }
  return p: future()
}

function any = |futures| {
  let size = futures: size()
  let p = promise()
  let i = java.util.concurrent.atomic.AtomicInteger(0)
  foreach f in futures {
    f: onSet(|v| -> p: set(v)): onFail(|e| {
      if i: incrementAndGet() == size {
        p: fail(java.util.NoSuchElementException())
      }
    })
  }
  return p: future()
}

