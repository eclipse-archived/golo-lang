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

