module gololang.testing.Utils


struct Test = { description, test }
struct Suite = { description, tests }
struct TestResult = { description, result }

function run = |suites| {
  java.lang.System.exit(defaultReporter(defaultRunner(suites)))
}

let defaultRunner = asFunctionalInterface(gololang.testing.TestRunner.class, |suites| -> list[
  [desc, match {
    when isClosure(test) then trying(test)
    otherwise defaultRunner(test)
  }]
  foreach desc, test in suites
])


let defaultReporter = asFunctionalInterface(gololang.testing.TestReporter.class, |results| {
  var err = 0
  foreach desc, result in results {
    if result oftype gololang.error.Result.class {
      print(desc + " " + result: either(
        |v| -> "OK",
        |e| -> "FAIL: " + e: message()))
      if result: isError() {
        err = err + 1
      }
    } else {
      println("# " + desc)
      err = err + defaultResult(result)
    }
  }
  return err
}

let defaultResult = |result| -> 1
