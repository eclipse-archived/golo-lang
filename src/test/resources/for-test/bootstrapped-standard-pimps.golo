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
