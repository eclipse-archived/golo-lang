module golotest.execution.AnonCall

function with_invoke = {
  return (|x| -> x + 1): invokeWithArguments(1)  
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

function main = |args| {
  require(with_invoke() == 2, "err") 
  require(direct_call() == 2, "err") 
  require(ident() == 2, "err") 
  require(anon_ident() == 2, "err")
  require(currified() == 2, "err")
}
