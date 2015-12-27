module gololang.ReporterSpec

import gololang.testing.Presenter
import gololang.testing.Runner
import gololang.Testing

function ListPresenter = |eventStore| {
  return Presenter()
  :onTestStarted(|t| -> eventStore: add(t: description() + " started"))
  :onTestDone(|t| -> eventStore: add(t: description() + " done"))
  :onSuiteStarted(|s| -> eventStore: add(s: description() + " started"))
  :onSuiteDone(|s| -> eventStore: add(s: description() + " done"))
  :onGlobalStarted({eventStore: add("Global started")})
  :onGlobalDone({eventStore: add("Global done")})
}

function dummyRunner = |eventStore| {
  let runner = build()
  runner: addPresenter(ListPresenter(eventStore))
  return runner
}

function spec = |$| {

  $: describe("A reporter", {

    $: it("should trigger events in order", {
      let events = list[]
      let fn = { events: add("<code>") }
      let r = dummyRunner(events)
      r: describe("Suite 1", {
        r: it("Test A", fn)
        r: describe("Suite 2", {
          r: it("Test B", fn)
        })
        r: it("Test C", fn)
      })

    r: describe("Suite 3", {})
    r: run()

    let expected = list[]
    :append("Global started")
    :append("Suite 1 started")
    :append("Test A started")
    :append("<code>!")
    :append("Test A done")
    :append("Suite 2 started")
    :append("Test B started")
    :append("<code>")
    :append("Test B done")
    :append("Suite 2 done")
    :append("Test C started")
    :append("<code>")
    :append("Test C done")
    :append("Suite 1 done")
    :append("Suite 3 started")
    :append("Suite 3 done")
    :append("Global done")

    require(events: size() == expected: size(), "should math expected size")
    for (var i = 0, i < events: size(), i = i + 1) {
      require(events: get(i) == expected: get(i), "expected is " + expected: get(i) + " actual is " + events: get(i))
    }

  })
})
}