module golotest.execution.Structs

import java.util.ArrayList

# ............................................................................................... #

struct Contact = { name, email }

function mrbean = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  return bean: name() + " <" + bean: email() + ">"
}

function mrbean_struct = -> Contact(): name("Mr Bean"): email("mrbean@outlook.com")

function mrbean_toString = {
  return Contact(): name("Mr Bean"): email("mrbean@outlook.com"): toString()
}

function mrbean_copy = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  return [bean, bean: copy()]
}

function mrbean_frozenCopy = {
  let bean = Contact(): name("Mr Bean"): email("mrbean@outlook.com")
  return [bean, bean: frozenCopy()]
}

function immutable_factory = {
  return [ImmutableContact("Mr Bean", "mrbean@outlook.com"), Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy()]
}

function mrbean_hashCode = -> [
  Contact("Mr Bean", "mrbean@outlook.com"),
  Contact("Mr Bean", "mrbean@outlook.com"),
  Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy(),
  Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy()
]

function mrbean_equals = -> [
  Contact("Mr Bean", "mrbean@outlook.com"),
  Contact("Mr Bean", "mrbean@outlook.com"),
  Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy(),
  Contact("Mr Bean", "mrbean@outlook.com"): frozenCopy(),
  Contact("Mr Bean", "mrbean@gmail.com"): frozenCopy(),
  Contact("Mr Beanz", "mrbean@outlook.com"): frozenCopy()
]

# ............................................................................................... #

struct FooBarBaz = { foo, _bar, baz }

augment golotest.execution.Structs.types.FooBarBaz {
  function leak = |this| -> this: _bar()
}

function fun_foo_bar_baz = -> FooBarBaz(): foo(1): _bar(2): baz(3)

function augmented_foo_bar_baz = -> FooBarBaz(1, 2, 3): leak()

# ............................................................................................... #

struct Point = { x, y }

augment Point {
  function str = |this| -> "{x=" + this: x() + ",y=" + this: y() + "}"
}

function check_concision = -> Point(1, 2): str()

# ............................................................................................... #

struct Couple = { x, y }

function check_compare = -> 
  Couple(1, 2) < Couple(1, 3)
  and Couple(1, 10) < Couple(2, 1)

function check_equals = -> 
  Couple(1, 2) != Couple(1, 2) 
  and ImmutableCouple(1, 2) == ImmutableCouple(1, 2)

function check_not_comparable = -> Couple(1, 2) < Point(1, 3)

struct AList = { _holder }

augment golotest.execution.Structs.types.AList {
  function add = |this, value| -> this: _holder(): add(value)
  function size = |this| -> this: _holder(): size()
}

function AList = -> golotest.execution.Structs.types.AList(ArrayList())

function check_overload_factory = -> AList(): size()
