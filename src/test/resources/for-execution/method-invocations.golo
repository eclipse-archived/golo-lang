module golotest.execution.MethodInvocations

import fr.insalyon.citi.golo.compiler.testing.support

function hello = {
  return "Hello": toString()
}

function a_list = |a, b| {
  let list = java.util.LinkedList()
  list: add(a)
  list: add(b)
  return list
}

function str_build = {
  return java.lang.StringBuilder("h"):
    append("e"):
    append("l"):
    append("l"):
    append("o"):
    toString()
}

function element_at = |list, index| {
  return list: get(index)
}

function toString_by_reflection = |obj| {
  return obj: getClass(): getMethod("toString"): invoke(obj)
}

function escaped = |a, b| {
  let helper = GoloTestHelperMethods()
  return helper: `not(helper: `is(a, b))
}

function sum_one_to_ten = {
  let interval = range(1, 11): incrementBy(1)
  var sum = 0
  foreach (i in interval) {
    sum = sum + i
  }
  return sum
}

function field_accessors = {
  let helper = GoloTestHelperFields()
  helper: field("wtf"): field("foo")
  return helper: field()
}

function access_items_from_subclass = {
  fr.insalyon.citi.golo.compiler.testing.support.SomeClass.FOO()
  let receiver = SomeClass()
  receiver: c()
  receiver: a()
  receiver: b()
}

function elvis_direct = -> null ?: plop()

function elvis_indirect = {
  let map = java.util.HashMap()
  map: put("foo", "-")
  map: put("bar", null)
  var str = map: get("foo") ?: toString()
  str = str + map: get("bar") ?: toString()
  return str
}

function funky = {
  let obj = DynamicObject():
    define("adder", |this, x| -> |y| -> |z| -> x + y + z)
  return obj: adder(1)(2)(3)
}
