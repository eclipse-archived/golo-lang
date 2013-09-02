module Closures

function a = {
  let g = |value| {
    let x = value
    println(x)
  }
  return g(2)
}

function b = |factor| {
  return |i| { return i * factor }
}

function c = {
  return {
    return 123
  }
}

function d = {
  let foo = |plop| {
    return plop + "!"
  }
  for (var i = 0, i < 10, i = i + 1) {
    foo(i)
  }
}

function compact = |a| -> a

function e = {
  let foo = 3
  return |x| -> x + foo
}

function f = |a| {
  return |x| -> a + x
}

function g = |a, b| -> a + b

function h = {
  let handler = |x| -> println("plop")
}

function i = {
  let adder = |a, b| -> a + b
  println(adder(1, 2))
}

function j = |a| -> |b| -> a + b

function k = {
  let map1 = HashMap()
  let map2 = HashMap()
  map1: put("put_twice", |x| -> map2: put(x, x * 2))
}

function l = {
  let f1 = ^k
  let f2 = ^foo.bar::baz
}

function m = {
  let a = 666
  let f1 = -> a
  let f2 = {
    return f1()
  }
  let f3 = {
    return {
      return f2()
    }
  }
  println(f1())
  println(f2())
}

function n = {
  let runner = {
    let a = "MrBean"
    let go = {
      let Person = |name, email| -> DynamicObject(): name(name): email(email)
      let f3 = {
        return |b| {
          return {
            return Person(a, b)
          }
        }
      }
      return f3(): invokeWithArguments("bean@outlook.com"): invokeWithArguments()
    }
    let result = go()
    println(result: name())
    println(result: email())
  }
  runner()
}

function closure_with_varargs_and_capture = {
  let obj = fr.insalyon.citi.golo.runtime.MethodInvocationSupportTest$VarargsChecking()
  return |args...| -> obj: defaultConcat(args)
}

function self_ref = {
  let a = 1
  let fun = |n| {
    fun(n + a)
  }
}

function self_fib = {
  let fib = |n| {
    if n < 2 {
      return n
    } else {
      return fib(n - 1) + fib(n - 2)
    }
  }
}
