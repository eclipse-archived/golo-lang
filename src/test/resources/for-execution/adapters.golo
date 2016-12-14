module golotest.execution.JavaAdapters

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest


function serializable = {
  let conf = map[
    ["interfaces", ["java.io.Serializable"]]
  ]
  return AdapterFabric(): maker(conf): newInstance()
}

function runnable = {
#tag::runnable[]
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
  runner: run() # <1>
#end::runnable[]
  return result
}

function override_toString = {
#tag::overrideToString[]
  let conf = map[
    ["overrides", map[
      ["toString", |super, this| -> ">>> " + super(this)]
    ]]
  ]
  let val = AdapterFabric(): maker(conf): newInstance()
#end::overrideToString[]
  return val
}

function construct_arraylist = {
  let conf = map[
    ["extends", "java.util.ArrayList"]
  ]
  return AdapterFabric(): maker(conf): newInstance(list["foo", "bar", "baz"])
}

function test_add_arraylist = {
#tag::add_arraylist[]
  let carbonCopy = list[]                               # <1>
  let conf = map[
    ["extends", "java.util.ArrayList"],
    ["overrides", map[
      ["*", |super, name, args| {                       # <2>
        if name == "add" {
          if args: length() == 2 {
            carbonCopy: add(args: get(1))               # <3>
          } else {
            carbonCopy: add(args: get(1), args: get(2)) # <4>
          }
        }
        return super: spread(args)                      # <5>
      }
    ]]
  ]]
  let list = AdapterFabric(): maker(conf): newInstance()
  list: add("bar")
  list: add(0, "foo")
  list: add("baz")                                      # <6>
#end::add_arraylist[]
  assertThat(carbonCopy, contains("foo", "bar", "baz"))
}
