# ............................................................................................... #
#
# Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ............................................................................................... #

module gololang.Async

function promise = -> 
  gololang.concurrent.async.Promise()

function setFuture = |value| -> 
  gololang.concurrent.async.AssignedFuture.setFuture(value)

function failedFuture = |throwable| ->
  gololang.concurrent.async.AssignedFuture.failedFuture(throwable)

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

function reduce = |futures, init, reducer| {
  let p = promise()
  all(futures): onSet(|results| {
    var acc = init
    foreach result in results {
      if result oftype java.lang.Throwable.class {
        p: fail(result)
        return
      } else {
        acc = reducer(acc, result)
      }
    }
    p: set(acc)
  })
  return p: future()
}

