module golotest.execution.Closures

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
