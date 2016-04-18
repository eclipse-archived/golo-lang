module gololang.testing.Suite

import gololang.testing.Test

struct Suite = {
  description,
  parent,
  children,
  befores,
  afters,
  onStart,
  onDone,
  report
}

struct SuiteReport = {
 total,
 failures
}

function Suite = |description, parent, onStart, onDone| -> gololang.testing.Suite.types.Suite()
:description(description)
:parent(parent)
:children(list[])
:befores(list[])
:afters(list[])
:onStart(onStart)
:onDone(onDone)
:report(SuiteReport(0, 0))

augment gololang.testing.Suite.types.SuiteReport {
  function addExcutedTest = |this, child| {
    if child oftype gololang.testing.Suite.types.Suite.class {
      this: total( this: total() + child: report(): total() )
    } else {
      this: total( this: total() + 1)
    }
  }
  function addFailures = |this, child| {
    if child oftype gololang.testing.Suite.types.Suite.class {
      this: failures( this: failures() + child: report(): failures() )
    } else {
      this: failures( this: failures() + 1)
    }
  }
}

augment gololang.testing.Suite.types.Suite {

  function run = |this| {
    this: onStart()(this)
    this: children(): each(|child| {
      this: befores(): each(|before| -> before())
      child: run()
      this: report(): addExcutedTest(child)
      if child: failed() {
        this: report(): addFailures(child)
      }
      this: afters(): each(|after| -> after())
    })
    this: onDone()(this)
  }

  function add = |this, it| {
    it: parent(this)
    this: children(): add(it)
  }

  function failed = |this| -> this: report(): failures() isnt 0

  function addBeforeEach = |this, before| -> this: befores(): add(before)
  function addAfterEach = |this, after| -> this: afters(): add(after)
}