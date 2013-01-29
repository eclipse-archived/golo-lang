module gololang.StandardPimps

pimp java.lang.invoke.MethodHandle {
  function to = |this, interfaceClass| -> asInterfaceInstance(interfaceClass, this)
}

