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
  let obj = DynamicObject():
    define("fun1", |this, args...| {
      result: append("|")
      foreach arg in args {
        result: append(prefix): append(arg)
      }
    }):
    define("fun2", |this, str, args...| {
      result: append("["+str+"]")
      foreach arg in args {
        result: append(prefix): append(arg)
      }
    }):
    define("fallback", |this, name, args...| {
      result: append("[fallback:"+name+"]")
      foreach arg in args {
        result: append(prefix): append(arg)
      }
    })

  obj: fun1()
  obj: fun1(1)
  obj: fun1(2, 3)
  obj: fun1(array[4, 5])
  obj: fun1(array[])

  obj: fun2("foo", 1)
  obj: fun2("foo", 2, 3)
  obj: fun2("foo", array[4, 5])
  obj: fun2("foo", array[])

  obj: jhon_doe()
  obj: jhon_doe(2, 3)
  obj: jhon_doe(array[4, 5])
  obj: jhon_doe(array[])

  return result: toString()
}

function kinds = {
  let t = DynamicObject("Foo")

  require(t: hasKind("Foo"), "err with good hasKind")
  require(not t: hasKind("Plop"), "err with bad hasKind")
  require(t: sameKind(DynamicObject("Foo")), "err with good sameKind")
  require(not t: sameKind(DynamicObject("Plop")), "err with bad sameKind")
  return true
}

function checkToString = {
  let t = DynamicObject("Foo"): name("bar")
  require(t: toString() == "Foo{name=bar}", "err in default toString")

  t: define("toString", |this| -> "I'm a Foo named "+ this: name())
  require(t: toString() == "I'm a Foo named bar", "err in redefined toString")
  return true
}

function checkDelegate = {
  let t = DynamicObject("deleguee")
    : name("Zaphod")
    : define("sayHello", |this| -> "Hello, I'm " + this: name())
    : define("varargs", |this, a, b...| -> "Z" + a + b: asList())
    : define("multi", |this, a, b...| -> "Z" + a + b: asList())

  let s = DynamicObject("withFallback")
    : foo("bar")
    : define("varargs", |this, a, b...| -> "B" + a + b: asList())
    : fallback(DynamicObject.delegate(t))

  require(s: sayHello() == "Hello, I'm Zaphod", "err sayHello")
  require(s: name() == "Zaphod", "err name")
  require(s: foo() == "bar", "err foo")

  s: define("answer", 42)
  require(s: answer() == 42, "err answer")
  require(t: get("answer") is null, "err answer")

  s: prop1("a")
  require(s: prop1() == "a", "err prop1")
  require(t: get("prop1") is null, "err prop1")

  s: name("Trillian")
  require(s: name() == "Trillian", "err name 2")
  require(t: name() == "Zaphod", "err name 2")

  require(s: varargs(1, 2, 3) == "B1[2, 3]", "err varargs")
  require(s: multi(1, 2, 3) == "Z1[2, 3]", "err multi")

  require(s: plic() is null, "err not defined get")
  try {
    s: plop("a", "b")
    raise("err not defined call")
  } catch(e) {
    require(e oftype UnsupportedOperationException.class, "err not defined call")
  }
  return true
}
