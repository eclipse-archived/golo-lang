module sample.EnumsThreadState

import java.lang.Thread$State

function main = |args| {

  # Call the enum entry like a function
  let new = Thread$State.NEW()
  println("name=" + new: name() + ", ordinal=" + new: ordinal())
  println("-----------")

  # Walk through all enum entries
  foreach (element in toCollection(Thread$State.values())) {
    println("name=" + element: name() + ", ordinal=" + element: ordinal())
  }
}

