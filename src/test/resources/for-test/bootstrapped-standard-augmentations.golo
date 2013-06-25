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
  return list_data(): sort()
}

function list_sorted = {
  return list_data(): sorted()
}

function list_sort_reverse = {
  return list_data(): sort(java.util.Collections.reverseOrder())
}

function list_sorted_reverse = {
  return list_data(): sorted(java.util.Collections.reverseOrder())
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
function sets_map = -> set_data(): map(|v| -> v * 10): unmodifiableView()
function sets_reduce = -> set_data(): reduce(0, |acc, v| -> acc + v)

function sets_each = {
  let int = AtomicInteger(0)
  set_data(): each(|v| -> int: addAndGet(v))
  return int: get()
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
function maps_map = -> map_data(): map(|k, v| -> mapEntry(k, v * 10)): unmodifiableView()
function maps_reduce = -> map_data(): reduce("", |acc, k, v| -> acc + k + v)

function maps_each = {
  let int = AtomicInteger(0)
  map_data(): each(|k, v| -> int: addAndGet(v))
  return int: get()
}

# ............................................................................................... #

function str_format1 = -> "%s": format("plop")
function str_format2 = -> "%s %s": format("plop", "da plop")

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
