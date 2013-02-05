module golo.test.bootstrapped.StandardPimpsTests

import java.util
import java.util.concurrent
import java.util.concurrent.atomic

function method_handle_to = -> (-> "ok"): to(Callable.class)

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