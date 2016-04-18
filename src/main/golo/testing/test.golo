module gololang.testing.Test

struct Test = {
  description,
  fn,
  parent,
  onStart,
  onDone,
  status
}

function Test = |description, fn, parent, onStart, onDone| -> gololang.testing.Test.types.Test()
:description(description)
:fn(fn)
:parent(parent)
:onStart(onStart)
:onDone(onDone)
:status("not_started")

local function _failed = -> "failed"

augment gololang.testing.Test.types.Test {
  function run = |this| {
    this: onStart()(this)
    try {
      this: fn()()
    } catch (e) {
      this: status(_failed())
    }
    this: onDone()(this)
  }

  function failed = |this| -> this: status() is _failed()
}