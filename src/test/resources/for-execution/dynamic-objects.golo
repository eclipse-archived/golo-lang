module golotest.execution.DynamicObjects

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function test_get_value = {
  assertThat(DynamicObject(): define("foo", "foo"): foo(), `is("foo"))
}

function test_set_then_get_value = {
  assertThat(DynamicObject(): foo("foo"): foo(), `is("foo"))
}

function test_call_as_method = {
  assertThat(
    DynamicObject()
    : define("echo", |this, str| -> str)
    : echo("w00t"),
    `is("w00t"))
}

function test_person_to_str = {
  let bean = DynamicObject()
    : name("Mr Bean")
    : email("mrbean@outlook.com")
    : define("toString",
        |this| -> this: name() + " <" + this: email() + ">")
  assertThat(bean: toString(), `is("Mr Bean <mrbean@outlook.com>"))
}

function test_with_function_update = {
  let obj = DynamicObject()
    : define("value", 0)
    : define("operation", |this| -> this: value(this: value() + 1))
  foreach (i in range(0, 10)) {
    obj: operation()
  }
  obj: define("operation", |this| -> this: value(this: value() * 2))
  obj: operation()
  obj: operation()
  assertThat(obj: value(), `is(40))
}

function test_mixins = {
  let foo = DynamicObject():
    define("a", 1):
    define("b", |this, x| -> x + 1):
    define("c", |this| -> "plop")
  let bar = DynamicObject():
    define("a", |this| -> 2):
    define("c", "[plop]")
  let baz = foo: mixin(bar)
  assertThat(baz: a() + baz: b(1) + baz: c(), `is("4[plop]"))
}

function test_copying = {
  let foo = DynamicObject(): define("a", 1)
  let bar = foo: copy(): define("a", 2)
  assertThat(foo: a() + bar: a(), `is(3))
}

function test_mrfriz = {
  let foo = DynamicObject(): define("a", 1): freeze()
  assertThat(foo: a(), `is(1))
  try {
    foo: a(666)
    raise("freeze had no effect")
  } catch (e) {
    assertThat(e, isA(java.lang.IllegalStateException.class))
  }
}

function test_propz = {
  let props = DynamicObject():
    foo("foo"):
    bar("bar"):
    properties()
  var result = ""
  foreach (prop in props) {
    result = result + prop: getKey() + ":" + prop: getValue()
  }
  # Damn ordering on sets...
  assertThat(result, either(`is("foo:foobar:bar")): `or(`is("bar:barfoo:foo")))
}

function test_with_varargs = {
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

  assertThat(result: toString(),
    `is("||@1|@2@3|@4@5|[foo]@1[foo]@2@3[foo]@4@5[foo][fallback:jhon_doe][fallback:jhon_doe]@2@3"))
}

function test_kinds = {
  let t = DynamicObject("Foo")

  assertThat(t: hasKind("Foo"), `is(true))
  assertThat(t: hasKind("Plop"), `is(false))
  assertThat(t: sameKind(DynamicObject("Foo")), `is(true))
  assertThat(t: sameKind(DynamicObject("Plop")), `is(false))
}

function check_toString = {
  let t = DynamicObject("Foo"): name("bar")
  assertThat(t: toString(), `is("Foo{name=bar}"))

  t: define("toString", |this| -> "I'm a Foo named "+ this: name())
  assertThat(t: toString(), `is("I'm a Foo named bar"))
}

function check_delegate = {
  let t = DynamicObject("deleguee")
    : name("Zaphod")
    : define("sayHello", |this| -> "Hello, I'm " + this: name())
    : define("varargs", |this, a, b...| -> "Z" + a + b: asList())
    : define("multi", |this, a, b...| -> "Z" + a + b: asList())

  let s = DynamicObject("withFallback")
    : foo("bar")
    : define("varargs", |this, a, b...| -> "B" + a + b: asList())
    : fallback(DynamicObject.delegate(t))

  assertThat(s: sayHello(), `is("Hello, I'm Zaphod"))
  assertThat(s: name(), `is("Zaphod"))
  assertThat(s: foo(), `is("bar"))

  s: define("answer", 42)
  assertThat(s: answer(), `is(42))
  assertThat(t: get("answer"), `is(nullValue()))

  s: prop1("a")
  assertThat(s: prop1(), `is("a"))
  assertThat(t: get("prop1"), `is(nullValue()))

  s: name("Trillian")
  assertThat(s: name(), `is("Trillian"))
  assertThat(t: name(), `is("Zaphod"))

  assertThat(s: varargs(1, 2, 3), `is("B1[2, 3]"))
  assertThat(s: multi(1, 2, 3), `is("Z1[2, 3]"))

  assertThat(s: plic(), `is(nullValue()))
  try {
    s: plop("a", "b")
    raise("err not defined call")
  } catch(e) {
    assertThat(e, isA(UnsupportedOperationException.class))
  }
}

function test_isFrozen = {
  let o = DynamicObject()
  assertThat(o: isFrozen(), `is(false))
  o: freeze()
  assertThat(o: isFrozen(), `is(true))
}

function test_defined_frozen = {
  let o = DynamicObject(): freeze()
  try {
    o: define("answer", 42)
    raise("should fail")
  } catch(e) {
    assertThat(e, isA(IllegalStateException.class))
  }
}

function test_undefined_frozen = {
  let o = DynamicObject(): define("answer", 42): freeze()
  try {
    o: undefine("answer")
    raise("should fail")
  } catch(e) {
    assertThat(e, isA(IllegalStateException.class))
  }
}


