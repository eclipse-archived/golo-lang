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

  function appendValues = |this, head, tail...| {
    this: append(head)
    foreach (element in atoList(tail)) {
      this: append(element)
    }
    return this
  }

  function prependValues = |this, head, tail...| {
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

pimp java.util.Set {
   
  function include = |this, element| {
    this: add(element)
    return this
  }

  function exclude = |this, element| {
    this: remove(element)
    return this
  }

  function includeValues = |this, first, rest...| {
    this: add(first)
    foreach (element in atoList(rest)) {
      this: add(element)
    }
    return this
  }

  function excludeValues = |this, first, rest...| {
    this: remove(first)
    foreach (element in atoList(rest)) {
      this: remove(element)
    }
    return this
  }

  function containsValues = |this, first, rest...| {
    if not(this: contains(first)) {
      return false
    } else {
      foreach (element in atoList(rest)) {
        if not(this: contains(element)) {
          return false
        }
      }
    }
    return true
  }

  function unmodifiableView = |this| -> java.util.Collections.unmodifiableSet(this)
}

# ............................................................................................... #

pimp java.util.Map {

  function add = |this, key, value| {
    this: put(key, value)
    return this
  }

  function delete = |this, key| {
    this: remove(key)
    return this
  }

  function getOrElse = |this, key, replacement| {
    let value = this: get(key)
    if (value isnt null) {
      return value
    }
    if (replacement oftype java.lang.invoke.MethodHandle.class) {
      return replacement()
    } else {
      return replacement
    }
  }

  function unmodifiableView = |this| -> java.util.Collections.unmodifiableMap(this)
}

# ............................................................................................... #

