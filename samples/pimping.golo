module samples.Pimping

import java.util.LinkedList

pimp java.util.List {
  function with = |this, value| {
    this: add(value)
    return this
  }
}

pimp java.util.Collection {
  function doToEach = |this, func| {
    foreach (element in this) {
      func(element)
    }
  }
}

function main = |args| {
  let list = LinkedList(): with("foo"): with("bar"): with("baz")
  list: doToEach(|value| -> println(">>> " + value))
}
