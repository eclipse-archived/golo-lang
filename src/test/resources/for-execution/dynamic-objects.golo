module golotest.execution.DynamicObjects

function get_value = -> DynamicObject(): define("foo", "foo"): foo()

function set_then_get_value = -> DynamicObject(): foo("foo"): foo()

function call_as_method = -> DynamicObject():
  define("echo", |this, str| -> str):
  echo("w00t")

function person_to_str = {
  let bean = DynamicObject(): name("Mr Bean"): email("mrbean@outlook.com")
  bean: define("toString", |this| -> this: name() + " <" + this: email() + ">")
  return bean: toString()
}

function with_function_update = {
  let obj = DynamicObject(): define("value", 0)
  obj: define("operation", |this| -> this: value(this: value() + 1))
  foreach (i in range(0, 10)) {
    obj: operation()
  }
  obj: define("operation", |this| -> this: value(this: value() * 2))
  obj: operation()
  obj: operation()
  return obj: value()
}

function mixins = {
  let foo = DynamicObject():
    define("a", 1):
    define("b", |this, x| -> x + 1):
    define("c", |this| -> "plop")
  let bar = DynamicObject():
    define("a", |this| -> 2):
    define("c", "[plop]")
  let baz = foo: mixin(bar)
  return baz: a() + baz: b(1) + baz: c()
}

function copying = {
  let foo = DynamicObject(): define("a", 1)
  let bar = foo: copy(): define("a", 2)
  return foo: a() + bar: a()
}

function mrfriz = {
  let foo = DynamicObject(): define("a", 1): freeze()
  if (foo: a()) isnt 1 {
    raise("a() shall have been 1")
  }
  try {
    foo: a(666)
    return "freeze had no effect"
  } catch (e) {
    return match {
      when e oftype java.lang.IllegalStateException.class then "OK"
      otherwise "WTF"
    }
  }
}

function propz = {
  let props = DynamicObject():
    foo("foo"):
    bar("bar"):
    properties()
  var result = ""
  foreach (prop in props) {
    result = result + prop: getKey() + ":" + prop: getValue()
  }
  return result
}

function with_varargs = {
  let prefix = "@"
  let result = java.lang.StringBuilder()
  let obj = DynamicObject(): define("fun", |this, args...| {
    result: append("|")
    foreach arg in args {
      result: append(prefix): append(arg)
    }
  })
  obj: fun()
  obj: fun(1)
  obj: fun(2, 3)
  return result: toString()
}
