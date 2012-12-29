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
