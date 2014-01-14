module Dispatcher

function dispatch = |data| {
  var result = ""
  foreach element in data {
    result = element: toString()
  }
  return result
}

function closure_dispatch = |f, arg| -> f(arg)
