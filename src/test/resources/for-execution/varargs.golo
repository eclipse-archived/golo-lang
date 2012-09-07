module golotest.execution.Varargs

function var_arg_ed = |index, args...| {
  return aget(args, index)
}

function call_varargs = |index| {
  var_arg_ed(index, "plop")
  return var_arg_ed(index, "foo", "bar")
}

function play_and_return_666 = {
  let data = array(0, 1, 2, 3, 4, 5, 666)
  return var_arg_ed(1, aget(data, 5), aget(data, 6))
}
