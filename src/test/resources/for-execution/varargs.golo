module golotest.execution.Varargs

import java.util.Arrays

function var_arg_ed = |index, args...| {
  return aget(args, index)
}

function call_varargs = |index| {
  var_arg_ed(index, "plop")
  return var_arg_ed(index, "foo", "bar")
}

function play_and_return_666 = {
  let data = Array(0, 1, 2, 3, 4, 5, 666)
  return var_arg_ed(1, aget(data, 5), aget(data, 6))
}

function var_args_test = |str, args...| {
    return "["+str+"]"+deepToString(args)
}

function test_empty = -> var_args_test("foo")

function test_one_arg = -> var_args_test("foo", 1)

function test_two_args = -> var_args_test("foo", 1, 2)

function test_ten_args = -> var_args_test("foo", 1, 2, 3, 4, 5, 6, 7, 8, 9, 0)

function test_array = {
  let bar = array[1, 2, 3, 4, 5, 6, 7, 8, 9, 0]
  return var_args_test("foo", bar)
}

function test_arrays = {
  let bar = array[array[1, 2, 3, 4], array[5, 6, 7], array[8, 9], 0]
  return var_args_test("foo", bar)
}