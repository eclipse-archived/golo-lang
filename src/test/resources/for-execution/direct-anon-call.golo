module golotest.execution.AnonCall

struct MyStruct = { func }
augment MyStruct {
  function meth = |this| -> |a| -> a + 1
}

function with_invoke = {
  return (|x| -> x + 1): invoke(1)
}

function direct_call = {
  return (|x| -> x + 1)(1)
}

function ident = {
  let id = |x| -> x
  return id(|x| -> x + 1)(1)
}

function anon_ident = {
  return (|x| -> x)(|x| -> x + 1)(1)
}

function currified = {
  return (|x| -> |y| -> x + y)(1)(1)
}

function struct_field = {
  let s = MyStruct(|a| -> a + 1)
  return (s: func())(1)
}

function struct_method = {
  let s = MyStruct(1)
  return (s: meth())(1)
}

function dynamic = {
  let o = DynamicObject(): define("meth", |this| -> |a| -> a + 1)
  return (o: meth())(1)
}

function main = |args| {
  require(with_invoke() == 2, "err")
  require(direct_call() == 2, "err")
  require(ident() == 2, "err")
  require(anon_ident() == 2, "err")
  require(currified() == 2, "err")
  require(struct_field() == 2, "err")
  require(struct_method() == 2, "err")
  require(dynamic() == 2, "err")
}
