# ............................................................................................... #

module gololang.StandardPimps

# ............................................................................................... #

pimp java.lang.invoke.MethodHandle {
  function to = |this, interfaceClass| -> asInterfaceInstance(interfaceClass, this)
}

# ............................................................................................... #

pimp java.util.List {

  function append = |this, element| {
    this: add(element)
    return this
  }

  function prepend = |this, element| {
    this: add(0, element)
    return this
  }
  
  function insert = |this, index, element| {
    this: add(index, element)
    return this
  }

  function appendAll = |this, head, tail...| {
    this: append(head)
    foreach (element in atoList(tail)) {
      this: append(element)
    }
    return this
  }

  function prependAll = |this, head, tail...| {
    for (var i = alength(tail) - 1, i >= 0, i = i - 1) {
      this: prepend(aget(tail, i))
    }
    return this: prepend(head)
  }

  function head = |this| -> this: get(0)
  function tail = |this| -> this: subList(1, this: size())
  function unmodifiableView = |this| -> java.util.Collections.unmodifiableList(this)
}

# ............................................................................................... #

