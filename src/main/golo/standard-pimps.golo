module gololang.StandardPimps

pimp java.lang.invoke.MethodHandle {
  function to = |this, interfaceClass| -> asInterfaceInstance(interfaceClass, this)
}

pimp java.util.List {

  function append = |this, element| {
    this: add(element)
    return this
  }

  function prepend = |this, element| {
    this: add(0, element)
    return this
  }

  function head = |this| -> this: get(0)
  function tail = |this| -> this: subList(1, this: size())
}

