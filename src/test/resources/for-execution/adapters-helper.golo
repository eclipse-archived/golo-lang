module golotest.execution.JavaAdaptersHelper

import gololang.Adapters

function serializable = {
  return gololang.Adapters.Adapter(): interfaces(["java.io.Serializable"]): newInstance()
}

function runnable = {
  let result = array[1, 2, 3]
  let conf = Adapter(): interfaces(["java.io.Serializable", "java.lang.Runnable"])
    : implements("run", |this| {
        for (var i = 0, i < result: length(), i = i + 1) {
          result: set(i, result: get(i) + 10)
        }
    })
  let runner = conf: newInstance()
  runner: run()
  return result
}

function override_toString = {
  let conf = Adapter(): overrides("toString", |super, this| -> ">>> " + super(this))
  return conf: newInstance()
}

function construct_arraylist = {
  let conf = Adapter(): extends("java.util.ArrayList")
  return conf: newInstance(list["foo", "bar", "baz"])
}

function add_arraylist = {
  let carbonCopy = list[]
  let conf = Adapter(): extends("java.util.ArrayList")
    : overrides("*", |super, name, args| {
        if name == "add" {
          if args: length() == 2 {
            carbonCopy: add(args: get(1))
          } else {
            carbonCopy: add(args: get(1), args: get(2))
          }
        }
        return super: handle(): invokeWithArguments(args)
    })
  let list = conf: newInstance()
  list: add("bar")
  list: add(0, "foo")
  list: add("baz")
  return carbonCopy
}
