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

----
This module offers asynchronous programming helpers, especially execution context agnostic promises
and futures. The provided APIs are orthogonal to the execution strategy: it is up to you to execute
code from the same thread, from a separate thread, or by pushing new tasks to a service executor.

The functions and augmentations in this module often delegate to Java classes from
`gololang.concurrent.async`.
----
module gololang.Async

----
Returns a new promise. Promise objects have the following useful methods.

* `set(value)`: sets the promise value. The value is ignored if the promise has already been set.
* `fail(exception)`: set the value to an exception.
* `get()`: returns the promise value. It returns a bogus `null` value if the promise is still
  undefined.
* `blockingGet()`: waits until the promise is set or failed, and returns the value.
* `future()`: returns a new future object on a promise.
* `isResolved()` and `isFailed()` query the promise status.

Future objects have the following methods.

* `onSet(|v| {...})`: registers a callback when the value is set, or executes it right now if it
  has already been set.
* `onFail(|e| {...})`: registers a callback when the corresponding promise fails with an exception.
* `isResolved()`, `isFailed()` `get()` and `blockingGet()` delegate to the promise implementation.
----
function promise = -> 
  gololang.concurrent.async.Promise()

----
Augmentation on the base `Promise` objects provided by the `gololang.concurrent.async.Promise` Java
class.

The promise initialize method takes one argument, a callback with two parameters, resolve and reject.
Do something within the callback, then call resolve if everything worked, otherwise call reject:

    let myPromise = -> promise(): initialize(|resolve, reject| {
      if everythingTurnedOutFine is true {
        resolve("Stuff worked!")
      } else {
        reject(java.lang.Exception("Failed!"))
      }
    })

    myPromise()
      : onSet(|result| {
          println(result) # Stuff worked!
      })
      : onFail(|err| {
          println(err: getMessage()) # Failed!
      })
----
augment gololang.concurrent.async.Promise {
  function initialize = |this, closure| {
    closure(|data| -> this: set(data), |err| -> this: fail(err))
    return this: future()
  }
}

----
Returns a future set to `value`.
----
function setFuture = |value| -> 
  gololang.concurrent.async.AssignedFuture.setFuture(value)

----
Returns a failed future to a `throwable` exception.
----
function failedFuture = |throwable| ->
  gololang.concurrent.async.AssignedFuture.failedFuture(throwable)

----
Augmentation on the base `Future` objects provided by the `gololang.concurrent.async.Future` Java
class.
----
augment gololang.concurrent.async.Future {

  ----
  Returns a future whose value is mapped through the `fun` function. 
  
  If this future is set to `v`, then the returned future is set to `fun(v)`. If it fails, the
  returned future is also failed with the same exception.
  ----
  function map = |this, fun| {
    let p = promise()
    this: onSet(|v| -> p: set(fun(v))): onFail(|t| -> p: fail(t))
    return p: future()
  }

  ----
  Similar to `map`, except that `fun` returns a future, not a value.
  ----
  function flatMap = |this, fun| {
    let p = promise()
    this: onSet(|v| ->
      fun(v): onSet(|r| -> p: set(r)): onFail(|t| -> p: fail(t))
    ): onFail(|t| -> p: fail(t))
    return p: future()
  }

  ----
  Returns a future that filters this future through the `pred` predicate function.

  Suppose that this future is set to `v`:

  * if `pred(v)` is `true`, then the result future is set to `v`,
  * if `pred(v)` is `false`, then the result is failed to a `java.util.NoSuchElementException`.

  If this future fails, so does the returned future.
  ----
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

  ----
  Returns a fallback future:

  * when this future is set, the returned future is set to the same value,
  * when it fails, the returned future matches the success or failure of `future`.
  ----
  function fallbackTo = |this, future| {
    let p = promise()
    this: onSet(|v| -> p: set(v)): onFail(|t| {
      future: onSet(|v| -> p: set(v)): onFail(|e| -> p: fail(e))
    })
    return p: future()
  }
}

----
Given a collection of futures, returns a future whose value is eventually a vector with the
results of these futures.

Given:

    all([ setFuture(1), failedFuture(e) ])

this yields a future whose eventual value is:

    vector[1, e]

Results are accumulated as futures get resolved. The last completed future triggers the calls to
`onSet`-registered listeners on the same thread.
----
function all = |futures| {
  let size = futures: size()
  let vector = java.util.ArrayList(size)
  let p = promise()
  let c = java.util.concurrent.atomic.AtomicInteger(0)
  let trigger = {
    if c: incrementAndGet() == size {
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

----
Given a collection of futures, returns a future whose value is set to the first completing future.

If all futures fail, then the returned future fails to a `java.util.NoSuchElementException`.
----
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

----
Returns a future whose value is set to the *reduction* of a collection of futures.

* `futures` is a collection of futures, and
* `init` is the initial value, and
* `reducer` is the reducing function of the form `|acc, next| {...}`.

If any future fails, then the result future fails, too. Otherwise, the returned future is set to the
accumulation of the values. Listeners callbacks get executed on the thread of the completing future
which is either the last successful future or the first future to fail.
----
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

----
Bridge structure to hold a reference to a Golo future and a Java future.

Instances of this struct are being returned by the `enqueue` augmentation on `ExecutorService`
instances. This essentially adds the ability to:

* use the Golo future for its composability, and
* use tha Java future to cancel a job.
----
struct FutureBridge = {
  _goloFuture,
  _javaFuture
}

----
A set of forwarding augmentations for `FutureBridge` instances.

The provided functions all forward to Golo futures, while `cancel` forwards to a Java future.
----
augment gololang.Async.types.FutureBridge {

  function onSet = |this, listener| -> 
    this: _goloFuture(): onSet(listener)

  function onFail = |this, listener| -> 
    this: _goloFuture(): onFail(listener)

  function map = |this, fun| -> 
    this: _goloFuture(): map(fun)

  function flatMap = |this, fun| ->
    this: _goloFuture(): flatMap(fun)

  function filter = |this, pred| -> 
    this: _goloFuture(): filter(pred)

  function fallbackTo = |this, future| -> 
    this: _goloFuture(): fallbackTo(future)

  function cancel = |this, mayInterruptIfRunning| -> 
    this: _javaFuture(): cancel(mayInterruptIfRunning)
}

----
Augmentations for `ExecutorService`.
----
augment java.util.concurrent.ExecutorService {

  ----
  Submits a function `fun` to be executed by this scheduler, and returns a `FutureBridge`.

  `fun` takes no parameters, and its return value is used as a future value.

  The returned `FutureBridge` behaves both as a composable Golo future and as a Java future that can be
  cancelled.

  Here is a sample usage:

      # Enqueue some elaborated work
      let f = executor: enqueue({
        Thread.sleep(1000_L)
        return 666
      })

      # Watch what could happen
      f: onSet(|v| -> println(v)): 
         onFail(|e| -> println(e: getMessage()))

      # ...but make it fail unless the CPU was too slow
      f: cancel(true)
  ----
  function enqueue = |this, fun| {
    let callable = fun: to(java.util.concurrent.Callable.class)
    let javaFuture = this: submit(callable)
    let result = promise()
    this: submit({
      try {
        result: set(javaFuture: get())
      } catch (e) {
        result: fail(e)
        if e oftype java.lang.InterruptedException.class {
          java.lang.Thread.currentThread(): interrupt()
        }
      }
    })
    return ImmutableFutureBridge(result: future(), javaFuture)
  }
}

