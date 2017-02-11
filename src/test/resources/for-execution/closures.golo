module golotest.execution.Closures

import java.awt.event
import java.util.HashMap
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.Objects

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
  return f: invoke(10)
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
  let array = array["boo"]
  let handler = |event| {
    array: set(0, event: getSource() + " -> " + event: getActionCommand())
  }
  let listener = asInterfaceInstance(ActionListener.class, handler)
  listener: actionPerformed(ActionEvent("Plop", 666, "da plop"))
  return array: get(0)
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
  map1: get("put_twice"): invoke(2)
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

function local_overloaded_fun = |a, b| -> a + b

function local_overloaded_fun = |a| -> a + 1

function call_local_overloaded_fun_with_arity1 = {
  let f = fun("local_overloaded_fun", golotest.execution.Closures.module, 1)
  return f(2)
}

function call_local_overloaded_fun_with_arity2 = {
  let f = fun("local_overloaded_fun", golotest.execution.Closures.module, 2)
  return f(1, 2)
}

function call_local_overloaded_fun_without_arity = {
  let f = fun("local_overloaded_fun", golotest.execution.Closures.module)
  return f(1, 2)
}

function call_local_overloaded_fun_short_literal = {
  let f = ^local_overloaded_fun
  return f(1, 2)
}

function call_local_overloaded_fun_full_literal = {
  let f = ^golotest.execution.Closures::local_overloaded_fun
  return f(1, 2)
}

function call_java_func_literal = {
  let f = ^java.util.Objects::isNull
  return [ f(null), f("42") ]
}

function call_imported_java_func_literal = {
  let f = ^isNull
  return [ f(null), f("42") ]
}

function nonNull = |o| -> match {
  when o is null then "n"
  otherwise "o"
}

function call_imported_overridden_java_func_literal = {
  let f = ^nonNull
  return [ f(null), f("42") ]
}

function call_java_method_literal_arity2 = {
  let f = ^String::endsWith: bindAt(1, "o")
  return list[f("Hello"), f("Goodbye"), f("Foo"), f("Bar")]
}

function call_java_method_literal = {
  let f = ^String::length
  return list[f("Hello"), f("Goodbye"), f("Foo"), f("Bar")]
}

function plop = |a| -> "p"
function plop = |a...| -> "pv"

function call_varargs_overloaded_fun = {
  let f = fun("plop", golotest.execution.Closures.module, 1, false)
  let fv = fun("plop", golotest.execution.Closures.module, 1, true)
  return [f(1), fv(1)]
}

function call_varargs_overloaded_fun_literal = {
  let f = ^plop\1
  let fv = ^plop\1...
  return [f(1), fv(1)]
}


function call_local_overloaded_fun_literal_with_arity1 = {
  let f = ^golotest.execution.Closures::local_overloaded_fun\1
  return f(2)
}

function call_local_overloaded_fun_literal_with_arity2 = {
  let f = ^golotest.execution.Closures::local_overloaded_fun\2
  return f(1, 2)
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

function closure_with_varargs_array_and_capture = {
  let prefix = "> "
  let fun = |args...| {
    var result = 0
    for (var i = 0, i < args: length(), i = i + 1) {
      result = result + args: get(i)
    }
    return prefix + result
  }
  let a = array[1, 2, 3]
  return fun(a)
}

function closure_with_trailing_varargs_and_capture = {
  let prefix = "|"
  let f = -> |head, tail...| {
    let result = java.lang.StringBuilder(): append(prefix): append(head)
    foreach element in tail {
      result: append(element)
    }
    return result: toString()
  }
  let g = f()
  return g(1) + g(1, 2) + g(1, 2, 3)
}

function closure_with_trailing_varargs_array_and_capture = {
  let prefix = "|"
  let f = -> |head, tail...| {
    let result = java.lang.StringBuilder(): append(prefix): append(head)
    foreach element in tail {
      result: append(element)
    }
    return result: toString()
  }
  let g = f()
  let a1 = array[2]
  let a2 = array[2, 3]
  return g(1) + g(1, a1) + g(1, a2)
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

function closure_self_reference = {
  let fun = |n| {
    if n < 3 {
      return fun(n + 1)
    } else {
      return 1
    }
  }
  return fun(0)
}

function funky = {
  let adder = |x| -> |y| -> |z| -> x + y + z
  return adder(1)(2)(3)
}

function closure_with_named_args = {
  let create_post = |title, body| -> title + " " + body
  return create_post(body = "Rocks", title = "It")
}

function closure_self_reference2 = {
  let fun = |x| -> match {
    when x is null then fun
    otherwise x
  }
  return fun(null)
}
