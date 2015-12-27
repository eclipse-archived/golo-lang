module gololang.testing.Reporter

struct Reporter = {
  presenters
}

function Reporter = -> gololang.testing.Reporter.types.Reporter(list[])

augment gololang.testing.Reporter.types.Reporter {

  function addPresenter = |this, presenter| { this: presenters(): add(presenter) }

  function onTestStarted = |this| -> |test| -> this: presenters(): each(|p| -> p: onTestStarted()(test))

  function onTestDone = |this| -> |test| -> this: presenters(): each(|p| -> p: onTestDone()(test))

  function onSuiteStarted = |this| -> |suite| -> this: presenters(): each(|p| -> p: onSuiteStarted()(suite))

  function onSuiteDone = |this| -> |suite| -> this: presenters(): each(|p| -> p: onSuiteDone()(suite))

  function onGlobalStarted = |this, runner| -> this: presenters(): each(|p| -> p: onGlobalStarted()(runner))

  function onGlobalDone = |this, runner| -> this: presenters(): each(|p| -> p: onGlobalDone()(runner))
}