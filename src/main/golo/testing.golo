module gololang.Testing

import gololang.testing.Runner
import gololang.testing.Suite
import gololang.testing.Test
import gololang.testing.Reporter

augment gololang.testing.Runner.types.Runner {

  function describe = |this, description, fn| {
    let parent = this: currentSuite()
    let suite = Suite(
      description,
      parent,
      this: reporter(): onSuiteStarted(),
      this: reporter(): onSuiteDone()
    )
    parent: add(suite)
    this: currentSuite(suite)
    fn()
    this: currentSuite(parent)
  }

  function it = |this, description, fn| {
    let parent = this: currentSuite()
    parent: add(Test(
      description,
      fn,
      parent,
      this: reporter(): onTestStarted(),
      this: reporter(): onTestDone()
    ))
  }

  function beforeEach = |runner, fn| -> runner: currentSuite(): addBeforeEach(fn)
  function afterEach = |runner, fn| -> runner: currentSuite(): addAfterEach(fn)
}