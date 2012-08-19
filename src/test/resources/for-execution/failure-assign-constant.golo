module golotest.execution.AssignToConstant

function some_func = {
  let foo = "foo"
  var bar = foo
  bar = "baz"
  foo = bar
}
