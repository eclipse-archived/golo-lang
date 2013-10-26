# ............................................................................................... #

module golo.test.bootstrapped.StandardAugmentationsTests

# ............................................................................................... #

import java.util
import java.util.concurrent
import java.util.concurrent.atomic

# ............................................................................................... #

function method_handle_to = -> (-> "ok"): to(Callable.class)

function lbind = -> (|a, b| -> a - b): bindAt(0, 10)
function rbind = -> (|a, b| -> a - b): bindAt(1, 10)

function chaining = -> (|x| -> x + 1): andThen(|x| -> x - 10): andThen(|x| -> x * 100)

# ............................................................................................... #

local function list_data = {
  return LinkedList():
    insert(0, 2):
    append(3, 4):
    prepend(0, 1)
}

function lists_filter = -> list_data(): filter(|v| -> (v % 2) is 0): unmodifiableView()

function list_find = -> list_data(): find(|v| -> v is 2)

function list_not_find = -> list_data(): find(|v| -> v is "a")

function lists_map = -> list_data(): map(|v| -> v * 10): unmodifiableView()

function lists_reduce = {
  let list = list_data()
  return list: head() + list: reduce(0, |acc, v| -> acc + v)
}

function lists_each = {
  let int = AtomicInteger(0)
  list_data(): each(|v| -> int: addAndGet(v))
  return int: get()
}

function list_reverse = {
  return list_data(): reverse()
}

function list_reversed = {
  return list_data(): reversed()
}

function list_sort = {
  return list_data(): order()
}

function list_sorted = {
  return list_data(): ordered()
}

function list_sort_reverse = {
  return list_data(): order(java.util.Collections.reverseOrder())
}

function list_sorted_reverse = {
  return list_data(): ordered(java.util.Collections.reverseOrder())
}

function list_count = {
 return list_data(): count(|item| -> item >= 2)
}

function list_count_zero = {
 return list_data(): count(|item| -> item >= 5)
}

function list_exists = {
 return list_data(): exists(|item| -> item == 2)
}

function list_not_exists = {
 return list_data(): exists(|item| -> item >= 5)
}

# ............................................................................................... #

local function set_data = {
  return HashSet():
    include(0):
    include(1, 2, 3, 4, 5, 6, 7):
    exclude(7):
    exclude(5, 6)
}

function sets_has_single = -> set_data(): has(2)
function sets_has_single_not = -> set_data(): has(10)

function sets_has_many = -> set_data(): has(2, 3)
function sets_has_many_not = -> set_data(): has(2, 30)

function sets_filter = -> set_data(): filter(|v| -> (v % 2) is 0): unmodifiableView()

function sets_find = -> set_data(): find(|v| -> v is 3)
function sets_not_find = -> set_data(): find(|v| -> v is 10)

function sets_map = -> set_data(): map(|v| -> v * 10): unmodifiableView()
function sets_reduce = -> set_data(): reduce(0, |acc, v| -> acc + v)

function sets_each = {
  let int = AtomicInteger(0)
  set_data(): each(|v| -> int: addAndGet(v))
  return int: get()
}

function sets_count = {
 return set_data(): count(|item| -> item >= 2)
}

function sets_count_zero = {
 return set_data(): count(|item| -> item >= 5)
}

function sets_exists = {
 return set_data(): exists(|item| -> item == 2)
}

function sets_not_exists = {
 return set_data(): exists(|item| -> item >= 5)
}

# ............................................................................................... #

local function map_data = {
  return TreeMap():
    add("a", 1):
    add("b", 2):
    add("c", 3)
}

function maps_addIfAbsent = -> map_data(): addIfAbsent("b", 666): get("b")
function maps_getOrElse = -> map_data(): delete("b"): unmodifiableView(): getOrElse("b", 666)

function maps_filter = -> map_data(): filter(|k, v| -> k isnt "b")

function maps_find = -> map_data(): find(|k, v| -> k is "a")
function maps_not_find = -> map_data(): find(|k, v| -> k is "d")

function maps_map = -> map_data(): map(|k, v| -> mapEntry(k, v * 10)): unmodifiableView()
function maps_reduce = -> map_data(): reduce("", |acc, k, v| -> acc + k + v)

function maps_each = {
  let int = AtomicInteger(0)
  map_data(): each(|k, v| -> int: addAndGet(v))
  return int: get()
}

function maps_count = {
 return map_data(): count(|key, item| -> item >= 2)
}

function maps_count_zero = {
 return map_data(): count(|key, item| -> item >= 5)
}

function maps_exists = {
 return map_data(): exists(|key, item| -> item == 2)
}

function maps_not_exists = {
 return map_data(): exists(|key, item| -> item >= 5)
}

# ............................................................................................... #

function str_format1 = -> "%s": format("plop")
function str_format2 = -> "%s %s": format("plop", "da plop")

function str_to_int = -> "40": toInt() + 2
function str_to_integer = -> "40": toInteger() + 2

function str_to_double = -> "-40.25": toDouble() + 82.25

function str_to_float = -> "1.42e-42": toFloat()

function str_to_long = -> "424242424242": toLong()

# ............................................................................................... #

function number_repeaters = {
  let builder = java.lang.StringBuilder()
  2: times(-> builder: append("."))
  3: times(|i| -> builder: append(i))
  10: upTo(12, |i| -> builder: append(i))
  12: downTo(10, |i| -> builder: append(i))
  return builder: toString()
}

# ............................................................................................... #

local function tuple_data = -> [1, 2, 3, 4, 5]

function tupled = -> tuple_data(): filter(|n| -> (n % 2) == 0): map(|n| -> n * 10): reduce(0, |acc, n| -> acc + n)

function tuple_find = -> tuple_data(): find(|v| -> v is 4)
function tuple_not_find = -> tuple_data(): find(|v| -> v is 10)

function tuple_count = {
 return tuple_data(): count(|item| -> item >= 2)
}

function tuple_count_zero = {
 return tuple_data(): count(|item| -> item >= 6)
}

function tuple_exists = {
 return tuple_data(): exists(|item| -> item == 2)
}

function tuple_not_exists = {
 return tuple_data(): exists(|item| -> item >= 6)
}

# ............................................................................................... #

local function vector_data = -> vector[1, 2, 3, 4, 5]

function vector_count = {
 return vector_data(): count(|item| -> item >= 2)
}

function vector_count_zero = {
 return vector_data(): count(|item| -> item >= 6)
}

function vector_exists = {
 return vector_data(): exists(|item| -> item == 2)
}

function vector_not_exists = {
 return vector_data(): exists(|item| -> item >= 6)
}

# ............................................................................................... #