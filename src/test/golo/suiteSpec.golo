module gololang.SuiteSpec

import gololang.Testing
import gololang.testing.Runner
import gololang.testing.Suite
import gololang.testing.Test
import gololang.testing.Reporter
import gololang.testing.Utils


function dummyTest = |fn| -> gololang.testing.Test.Test("dummyTest", fn, null, NO_OP_1(), NO_OP_1())
function dummySuite = -> gololang.testing.Suite.Suite("dummySuite", null, NO_OP_1(), NO_OP_1())

function spec = |$| {

  $: describe("A suite", {

#    $: beforeEach({
#      println("Before each")
#    })

    $: it("should set a test parent to this when added", {
      let test = dummyTest({})
      let suite = dummySuite()
      require(suite: children(): isEmpty(), "The suite should be empty")
      suite: add(test)
      require(suite: children(): size() == 1, "The test should be added")
    })

    $: describe("Another one", {

    $: it("should run beforeEach before every single test", {
      let myList = list[]
      let before = { myList: add("before") }
      let test1 = dummyTest({myList: add("test1")})
      let test2 = dummyTest({myList: add("test2")})
      let suite = dummySuite()
      suite: add(test1)
      suite: add(test2)
      suite: addBeforeEach(before)
      suite: run()
      require(myList: join("-") == "before-test1-before-test2", "the befores should be before the tests")
    })

    })
#    $: afterEach({
#      println("After each")
#    })
  })
}