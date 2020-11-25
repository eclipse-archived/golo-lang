----
Tests for the `&within` macro and associated functions.
----
module golotest.control.context

import gololang.Control

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_simple = {
  let l = list[]
  let ctx = -> context(
    -> l: append("enter"),
    |l, e| {
      l: add("exit")
      l: append(e?: message())
      return e
    }
  )

  &within(c = ctx()) {
    c: add("within")
  }

  assertThat(l, contains("enter", "within", "exit", null))
}

function test_return = {
  let l = list[]
  let ctx = context(
    -> l: append("enter"),
    |l, e| {
      l: add("exit")
      return e
    }
  )

  let f = {
    &within(c = ctx) {
      return c: append("within")
    }
  }

  assertThat(f(), contains("enter", "within", "exit"))
}

function test_enter_fails = {
  let l = list[]
  let ctx = context(
    -> raise("err in enter"),
    |l, e| {
      l : add("exit")
      return e
    }
  )
  try {
    &within(c = ctx) {
      c: add("within")
    }
    raise("must fail")
  } catch (e) {
    assertThat(e: message(), `is("err in enter"))
    assertThat(l: isEmpty(), `is(true))
  }
}

function test_exit_fails = {
  let l = list[]
  let ctx = context(
    -> l: append("enter"),
    |l, e| -> raise("err in exit")
  )
  try {
    &within(c = ctx) {
      c: add("within")
    }
    raise("must fail")
  } catch (e) {
    assertThat(e: message(), `is("err in exit"))
    assertThat(l, contains("enter", "within"))
  }
}

function test_work_fails = {
  let l = list[]
  let ctx = context(
    -> l: append("enter"),
    |l, e| {
      l: append("exit")
      l: append(e?: message())
      return e
    }
  )
  try {
    &within(ctx) {
      raise("err inside")
    }
  } catch (e) {
    assertThat(e: message(), `is("err inside"))
    assertThat(l, contains("enter", "exit", "err inside"))
  }
}

function test_work_fails_exit_true = {
  let l = list[]
  let ctx = context(
    -> l: append("enter"),
    |l, e| {
      l: append("exit")
      l: append(e?: message())
      return e
    }
  )
  try {
    &within(c = ctx) {
      raise("err inside")
    }
  } catch (e) {
    assertThat(e: message(), `is("err inside"))
    assertThat(l, contains("enter", "exit", "err inside"))
  }
}

function test_work_fails_exit_false = {
  let l = list[]
  let ctx = context(
    -> l: append("enter"),
    |l, e| {
      l: append("exit")
      l: append(e?: message())
      return null
    }
  )
  &within(c = ctx) {
    raise("err inside")
  }
  assertThat(l, contains("enter", "exit", "err inside"))
}

function test_work_and_exit_fails = {
  let l = list[]
  let ctx = context(
    -> l: append("enter"),
    |l, e| { raise("err in exit") }
  )

  try {
    &within(ctx) {
      raise("err inside")
    }
  } catch (e) {
    assertThat(e: message(), `is("err inside"))
    assertThat(e: suppressed(): size(), `is(1))
    assertThat(e: suppressed(): get(0): message(), `is("err in exit"))
    assertThat(l, contains("enter"))
  }
}

function test_context_fails = {
  let ctx = { raise("err in ctx") }
  let l = list[]

  try {
    &within(ctx()) {
      l: add("inside")
    }
  } catch (e) {
    assertThat(e: message(), `is("err in ctx"))
    assertThat(l: isEmpty(), `is(true))
  }
}

function test_wrapp_exception_manual = {
  let l = list[]
  let ctx = context( -> l: append("enter"), |l, e| {
    l: add("exit")
    return RuntimeException("new message", e)
  })

  var err = null
  try {
    &within(ctx) {
      raise("inside")
    }
  } catch (e) {
    err = e
  }
  assertThat(err: message(), `is("new message"))
  assertThat(err: cause(): message(), `is("inside"))
}

function test_wrapped_exception = {
  var err = null
  try {
    &within(wrapped(IllegalArgumentException.class)) {
      raise("err inside")
    }
    raise("should fail")
  } catch (e) {
    err = e
  }
  assertThat(err, isA(IllegalArgumentException.class))
  assertThat(err: cause(): message(), `is("err inside"))
}

function test_redirect_output = {
  let outb = java.io.ByteArrayOutputStream()
  let out = java.io.PrintStream(outb)

  let std = System.out()

  &within(stdout(out)) {
    print("answer:")
  }

  &within(stderr(out)) {
    System.err(): print(" 42")
  }

  assertThat(outb: toString(), `is("answer: 42"))
  assertThat(System.out(), `is(std))
}

function test_closing_wrapper = {
  let l = list[]
  let res = DynamicObject(): define("close", |this| { l: add("closed") })

  &within(r=closing(res)) {
    assertThat(r, `is(res))
  }

  try {
    &within(r=closing(res)) {
      raise("err")
    }
  } catch(e) {
    assertThat(e: message(), `is("err"))
  }

  assertThat(l, contains("closed", "closed"))
}

function test_augmented_autoclose = {
  var l = list[]
  var ac = gololang.ControlTest.dummyClose(l, null)
  &within(ac) {
    l: add("inside")
  }
  assertThat(l, contains("inside", "closed"))

  l: clear()
  try {
    &within(ac) {
      raise("err inside")
    }
    raise("should have failed")
  } catch (e) {
    assertThat(e: message(), `is("err inside"))
  }
  assertThat(l, contains("closed"))

  l = list[]
  ac = gololang.ControlTest.dummyClose(l, "err while closing")
  var err = null
  try {
    &within(ac) {
      l: add("inside")
    }
  } catch (e) {
    err = e
  }
  assertThat(err?: message(), `is("err while closing"))
  assertThat(l, contains("inside"))
}

function test_augmented_lock = {
  var l = list[]
  var lc = gololang.ControlTest.dummyLock(l, null)
  &within(lc) {
    l: add("inside")
  }
  assertThat(l, contains("locked", "inside", "unlocked"))

  l: clear()
  try {
    &within(lc) {
      raise("err inside")
    }
    raise("should have failed")
  } catch (e) {
    assertThat(e: message(), `is("err inside"))
  }
  assertThat(l, contains("locked", "unlocked"))

  l = list[]
  lc = gololang.ControlTest.dummyLock(l, "err while unlocking")
  var err = null
  try {
    &within(lc) {
      l: add("inside")
    }
  } catch (e) {
    err = e
  }
  assertThat(err?: message(), `is("err while unlocking"))
  assertThat(l, contains("locked", "inside"))
}

function test_suppress_exception_context = {
  let ctx = suppress(IllegalStateException.class, IllegalArgumentException.class)
  var err = null
  try {
    &within(ctx) {
      throw IllegalStateException("err")
    }
  } catch (e) {
    err = e
  }
  assertThat(err, `is(nullValue()))

  try {
    &within(ctx) {
      throw IllegalArgumentException("err")
    }
  } catch (e) {
    err = e
  }
  assertThat(err, `is(nullValue()))

  try {
    &within(ctx) {
      throw RuntimeException("err")
    }
  } catch (e) {
    err = e
  }
  assertThat(err: message(), `is("err"))
}


function test_lock_unlock_wrapper = {
  let lst = list[]
  let lock = DynamicObject()
    : define("lock", |this| { lst: add("locked") })
    : define("unlock", |this| { lst: add("unlocked") })

  &within(lc=locking(lock)) {
    lst: add("inside 1")
    &within(unlocking(lock)) {
      lst: add("inside 2")
    }
    lst: add("inside 3")
  }
  assertThat(lst, contains("locked", "inside 1", "unlocked", "inside 2", "locked", "inside 3", "unlocked"))

  lst: clear()
  var err = null
  try {
    &within(lc=locking(lock)) {
      lst: add("inside 1")
      &within(unlocking(lock)) {
        raise("err inside")
      }
      lst: add("inside 3")
    }
  } catch (e) {
    err = e
  }
  assertThat(err: message(), `is("err inside"))
  assertThat(lst, contains("locked", "inside 1", "unlocked", "locked", "unlocked"))

}

function test_nested_contexts = {
  let errorLog = exceptionFilter(|e| { print(e: localizedMessage()) })
  let outBuf = java.io.ByteArrayOutputStream()
  let out = java.io.PrintStream(outBuf)

  &within(stdout(out), errorLog) {
    raise("err inside")
  }
  assertThat(outBuf: toString(), `is("err inside"))
}

function test_chained_contexts = {
  let lst = list[]
  let outer = context(-> lst: append("enter outer"), |l, e| {
    l: add("exit outer")
    return e
  })
  let inner = |l| -> context(-> l: append("enter inner"), |t, e| {
    t: add("exit inner")
    return e
  })

  &within(l = outer, tgt = inner(l)) {
    tgt: add("inside")
  }
  assertThat(lst, contains("enter outer", "enter inner", "inside", "exit inner", "exit outer"))

  lst: clear()
  var err = null
  try {
    &within(l = outer, tgt = inner(l)) {
      raise("err inside")
    }
    raise("should fail")
  } catch (e) {
    err = e
  }
  assertThat(err: message(), `is("err inside"))
  assertThat(lst, contains("enter outer", "enter inner", "exit inner", "exit outer"))

}
