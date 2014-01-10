module Dispatcher

function dispatch = |data| {
  var result = ""
  foreach element in data {
    result = element: toString()
  }
  return result
}

function funky_function_handle = -> |obj| -> obj: toString()

function funky_function = -> funky_function_handle(): to(org.gololang.microbenchmarks.dispatch.FunkyFunction.class)
