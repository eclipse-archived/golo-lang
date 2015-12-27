module gololang.testing.Runner

import gololang.testing.Utils

import gololang.testing.Suite
import gololang.testing.Test
import gololang.testing.Reporter
import gololang.testing.presenters.Console

struct Runner = {
  currentSuite,
  reporter
}

function build = {
  let reporter = Reporter()
  let top_level_suite = Suite("TOP_LEVEL_SUITE", null, NO_OP_1(), NO_OP_1())
  let runner = Runner(top_level_suite, reporter)
  runner: addPresenter(Console())
  return runner
}

augment gololang.testing.Runner.types.Runner {
  function run = |this| {
    this: reporter(): onGlobalStarted(this)
    this: currentSuite(): run()
    this: reporter(): onGlobalDone(this)
  }

  function addPresenter = |this, presenter| -> this: reporter(): addPresenter(presenter)
}