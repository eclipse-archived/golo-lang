module golotest.execution.Closures

import java.awt.event
import java.util.HashMap
import java.util.concurrent.Callable
import java.util.concurrent.Executors

function raw_handle = {
  return |obj| {
    return obj: toString()
  }
}

function handle_with_capture = |a, b| {
  return |factor| {
    return (a + b) * factor
  }
}

function call_with_invoke = {
  let f = handle_with_capture(5, 4)
  return f: invokeWithArguments(10)
}

function call_with_ref = {
  let f = handle_with_capture(1, 2)
  return f(10)
}

function adder = |a, b| -> a + b

function add_to = |a| {
  return |x| -> a + x
}

function as_explicit_interface = {
  let array = Array("boo")
  let handler = |event| {
    aset(array, 0, event: getSource() + " -> " + event: getActionCommand())
  }
  let listener = asInterfaceInstance(ActionListener.class, handler)
  listener: actionPerformed(ActionEvent("Plop", 666, "da plop"))
  return aget(array, 0)
}

function executor_and_callable = {
  let executor = Executors.newSingleThreadExecutor()
  let future = executor: submit(asInterfaceInstance(Callable.class, -> "hey!"))
  let result = future: get()
  executor: shutdown()
  return result
}

function nested_compact = |a| -> |b| -> a + b

function in_a_map = {
  let map1 = HashMap()
  let map2 = HashMap()
  map1: put("put_twice", |x| -> map2: put(x, x * 2))
  map1: get("put_twice"): invokeWithArguments(2)
  return map2: get(2)
}

local function local_fun = |x| -> x + 1

function call_local_fun = {
  let f = fun("local_fun", golotest.execution.Closures.module)
  return f(1)
}

function call_local_fun_short_literal = {
  let f = ^local_fun
  return f(1)
}

function call_local_fun_full_literal = {
  let f = ^golotest.execution.Closures::local_fun
  return f(1)
}

function nested_closures = {
  let s = "plop"
  let f1 = |x| -> x
  let f2 = {
    return {
      return f1(s)
    }
  }
  return f2()
}

function closure_with_varargs_and_capture = {
  let prefix = "> "
  let fun = |args...| {
    var result = 0
    for (var i = 0, i < args: length(), i = i + 1) {
      result = result + args: get(i)
    }
    return prefix + result
  }
  return fun(1, 2, 3)
}

function closure_with_synthetic_refs = {
  let builder = java.lang.StringBuilder()
  let fun = {
    foreach (i in range(0, 3)) {
      builder: append(i)
    }
  }
  fun()
  return builder: toString()
}

function closure_with_synthetic_refs_in_match = {
  let fun = |x| -> match {
    when x: startsWith("1") then "1"
    when x: startsWith("2") then "2"
    otherwise "0"
  }
  return fun("12") + fun("21") + fun("666")
}

function scoping_check = {
  var acc = 100
  let delta = 4
  let f = {
    var acc = 0
    var delta = 0
    for (var i = 0, i <= 3, i = i + 1) {
      acc = acc + i
    }
    delta = 10
    return acc + delta
  }
  return acc + f() + delta
}
