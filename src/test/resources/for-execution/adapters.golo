module golotest.execution.JavaAdapters

function serializable = {
  let conf = map[
    ["interfaces", ["java.io.Serializable"]]
  ]
  return AdapterFabric(): maker(conf): newInstance()
}

function runnable = {
  let result = array[1, 2, 3]
  let conf = map[
    ["interfaces", ["java.io.Serializable", "java.lang.Runnable"]],
    ["implements", map[
      ["run", |this| {
        for (var i = 0, i < result: length(), i = i + 1) {
          result: set(i, result: get(i) + 10)
        }
      }]
    ]]
  ]
  let runner = AdapterFabric(): maker(conf): newInstance()
  runner: run()
  return result
}

function override_toString = {
  let conf = map[
    ["overrides", map[
      ["toString", |super, this| -> ">>> " + super(this)]
    ]]
  ]
  return AdapterFabric(): maker(conf): newInstance()
}

function construct_arraylist = {
  let conf = map[
    ["extends", "java.util.ArrayList"]
  ]
  return AdapterFabric(): maker(conf): newInstance(list["foo", "bar", "baz"])
}

function add_arraylist = {
  let carbonCopy = list[]
  let conf = map[
    ["extends", "java.util.ArrayList"],
    ["overrides", map[
      ["*", |super, name, args| {
        if name == "add" {
          if args: length() == 2 {
            carbonCopy: add(args: get(1))
          } else {
            carbonCopy: add(args: get(1), args: get(2))
          }
        }
        return super: spread(args)
      }
    ]]
  ]]
  let list = AdapterFabric(): maker(conf): newInstance()
  list: add("bar")
  list: add(0, "foo")
  list: add("baz")
  return carbonCopy
}
