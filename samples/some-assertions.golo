module some.assertions

import gololang.Assertions

struct Point = {
  x,y
}

function main = |args| {

  # be careful, if no callback and assertion is false, then the program is aborted
  assert(-> 5: equals(5))

  assert(-> "BoB" oftype String.class, |error| {
    println("Huston?")
  })

  assert(predicate= -> "BoB" oftype Integer.class,
    successMessage= "This is an Integer",
    onSuccess= |res| {
      println("You Win")
    },
    errorMessage= "This isn't an Integer",
    onError = |error| {
      println("Huston?")
    }
  )

  assert(predicate= -> 5 < 0, onSuccess=|res|{}, onError=|err|{
    println("5 is not less than 0!")
  })

  # be careful, if no callback and assertion is false, then the program is aborted
  assertEqual(42, 42)

  assertEqual(42, 69, |error| {
    println(error)
  })


  assertEqual(Point(5,5), Point(5,5), |error| {
    println("???")
  })

  let point = Point(5,5)
  assertEqual(point: frozenCopy(), point: frozenCopy(), |res| {
      println(":)")
    },
    |error| {
      println(":(")
  })

  gololang.Assertions.displayTestsReport()
}