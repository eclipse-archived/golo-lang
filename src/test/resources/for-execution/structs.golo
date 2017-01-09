module golotest.execution.Structs

import java.util.ArrayList
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.testng.Assert



# ............................................................................................... #

struct Contact = { name, email }

function check_accessors = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  assertThat(bean: name() + " <" + bean: email() + ">",
            equalTo("Mr Bean <mrbean@outlook.com>"))
}

function check_struct = {
  let s = Contact(): name("Mr Bean"): email("mrbean@outlook.com")

  var t = s: members()
  assertThat(t, instanceOf(Tuple.class))
  assertThat(t: size(), `is(2))
  assertThat(t: get(0), `is("name"))
  assertThat(t: get(1), `is("email"))

  t = s: values()
  assertThat(t: size(), `is(2))
  assertThat(t: get(0), `is("Mr Bean"))
  assertThat(t: get(1), `is("mrbean@outlook.com"))

  let structIterator = s: iterator()
  assertTrue(structIterator: hasNext())
  t = structIterator: next()
  assertThat(t: size(), `is(2))
  assertThat(t: get(0), `is("name"))
  assertThat(t: get(1), `is("Mr Bean"))
  assertTrue(structIterator: hasNext())
  t = structIterator: next()
  assertThat(t: size(), `is(2))
  assertThat(t: get(0), `is("email"))
  assertThat(t: get(1), `is("mrbean@outlook.com"))
  assertFalse(structIterator: hasNext())

  assertThat(s: get("name"), `is("Mr Bean"))
  assertThat(s: get("email"), `is("mrbean@outlook.com"))
  try {
    s: get("foo")
    fail("An IllegalArgumentException was expected")
  } catch(e) {
    assertThat(e, instanceOf(IllegalArgumentException.class))
  }

  let c = s: copy()
  c: set("name", "John")
  assertThat(c: get("name"), `is("John"))
  assertThat(s: get("name"), `is("Mr Bean"))
  try {
    c: set("foo", "bar")
    fail("An IllegalArgumentException was expected")
  } catch (e) {
    assertThat(e, instanceOf(IllegalArgumentException.class))
  }

  assertFalse(s: isFrozen())
  let f = s: frozenCopy()
  assertTrue(f: isFrozen())
  assertFalse(f: copy(): isFrozen())
  try {
    f: set("name", "John")
    fail("An IllegalStateException was expected")
  } catch (e) {
    assertThat(e, instanceOf(IllegalStateException.class))
  }
}

function check_toString = {
  assertThat(Contact(): name("Mr Bean"): email("mrbean@outlook.com"): toString(),
            equalTo("struct Contact{name=Mr Bean, email=mrbean@outlook.com}"))
}

function check_copy = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  let bean_copy = bean: copy()
  assertThat(bean: toString(), `is("struct Contact{name=Mr Bean, email=mrbean@outlook.com}"))
  assertThat(bean_copy: toString(), `is("struct Contact{name=Mr Bean, email=mrbean@outlook.com}"))
  assertThat(bean, `not(sameInstance(bean_copy)))
}

function check_frozenCopy = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  let fcopy = bean: frozenCopy()
  assertThat(bean: toString(), `is("struct Contact{name=Mr Bean, email=mrbean@outlook.com}"))
  assertThat(fcopy: toString(), `is("struct Contact{name=Mr Bean, email=mrbean@outlook.com}"))
  assertThat(bean, `not(sameInstance(fcopy)))
  try {
    fcopy: name("Foo")
    fail("A frozen struct shall not allow field mutation")
    bean: name("Foo")
  } catch (e) {
    assertThat(e, instanceOf(IllegalStateException.class))
  }
}

function check_immutable_factory = {
  assertThat(ImmutableContact("Mr Bean", "mrbean@outlook.com"),
             equalTo(Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy()))
}

function check_hashCode = {
  let r = [
    Contact("Mr Bean", "mrbean@outlook.com"),
    Contact("Mr Bean", "mrbean@outlook.com"),
    Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy(),
    Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy()
  ]
  assertThat(r: get(0): hashCode(), `not(equalTo(r: get(1): hashCode())))
  assertThat(r: get(2): hashCode(), `is(r: get(3): hashCode()))
}

function check_equals = {
  let c0 = Contact("Mr Bean", "mrbean@outlook.com")
  let c1 = Contact("Mr Bean", "mrbean@outlook.com")
  let c2 = Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy()
  let c3 = Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy()
  let c4 = Contact("Mr Bean", "mrbean@gmail.com"): frozenCopy()
  let c5 = Contact("Mr Beanz", "mrbean@outlook.com"): frozenCopy()
  assertThat(c0, `not(c1))
  assertThat(c0, `not(Object()))
  assertThat(c0, `not(c2))
  assertThat(c2, `is(c3))
  assertThat(c2, `not(c4))
  assertThat(c2, `not(c5))
  assertThat(c2, `not(c0))
}

# ............................................................................................... #

struct FooBarBaz = { foo, _bar, baz }

augment golotest.execution.Structs.types.FooBarBaz {
  function leak = |this| -> this: _bar()
}

function check_private_field = {
  let s = FooBarBaz(): foo(1): _bar(2): baz(3)

  assertThat(s: members(): size(), `is(2))
  assertThat(s: values(): size(), `is(2))
  let structIterator = s: iterator()
  assertTrue(structIterator: hasNext())
  assertThat(structIterator: next(), `is(["foo", 1]))
  assertTrue(structIterator: hasNext())
  assertThat(structIterator: next(), `is(["baz", 3]))
  assertFalse(structIterator: hasNext())
  assertThat(s: get("foo"), `is(1))
  assertThat(s: get("baz"), `is(3))
  try {
    s: get("_bar")
    fail("An IllegalArgumentException was expected")
  } catch (e) {
    assertThat(e, instanceOf(IllegalArgumentException.class))
  }
  assertThat(s: copy(): members(): size(), `is(2))
}

function check_augmented_access_private = {
  assertThat(FooBarBaz(1, 2, 3): leak(), `is(2))
}


# ............................................................................................... #

struct Point = { x, y }

augment Point {
  function str = |this| -> "{x=" + this: x() + ",y=" + this: y() + "}"
}

function check_concision = {
  assertThat(Point(1, 2): str(), equalTo("{x=1,y=2}"))
}

# ............................................................................................... #

struct Couple = { x, y }

function check_compare = {
  assertTrue(Couple(1, 2) < Couple(1, 3) and Couple(1, 10) < Couple(2, 1))
}

function check_equals_operators = {
  assertTrue(Couple(1, 2) != Couple(1, 2) and ImmutableCouple(1, 2) == ImmutableCouple(1, 2))
}

function check_not_comparable = {
  try {
    if Couple(1, 2) < Point(1, 3) {
      fail("Should have failed")
    }
  } catch(e) {
    assertThat(e, instanceOf(IllegalArgumentException.class))
    assertThat(e: getMessage(),
          containsString("struct Couple{x=1, y=2} and struct Point{x=1, y=3} can't be compared"))
  }
}

struct AList = { _holder }

augment golotest.execution.Structs.types.AList {
  function add = |this, value| -> this: _holder(): add(value)
  function size = |this| -> this: _holder(): size()
}

function AList = -> golotest.execution.Structs.types.AList(ArrayList())

function check_overload_factory = {
  let l = AList(): size()
  assertThat(l, instanceOf(Integer.class))
  assertThat(l, equalTo(0))
}
