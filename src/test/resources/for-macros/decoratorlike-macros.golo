module golo.test.decoratorMacros

import gololang.ir
import gololang.ir.DSL

local function prependResult = |block, value| {
  let ret = block: getStatements(): last()
  ret: replaceInParentBy(`return(plus(value, ret: expression())))
}

----
macro with side effect: change the IR
----
macro deco = |funct| {
  prependResult(funct: getBlock(), constant("deco "))
  return funct
}

----
macro with side effect (change the IR) with 2 parameters for decorator test
----
macro deco2 = |msg, funct| {
  prependResult(funct: getBlock(), constant("deco2 " + msg: value() + " "))
  return funct
}

----
(almost) Pure macro: return a new function without changing the original one.
----
macro deco3 = |funct| {
  return funct: sameSignature()
    : returns(
      plus(
        constant("deco3 "),
        lambda(): withParameters(funct: parameterNames())
        : block(funct: getBlock())
        : call(funct: parametersAsRefs())))
}

----
Macro injecting code in the enclosing module.
----
macro decoMod = |prefix, fn| {
  let mod = fn: enclosingModule()
  mod?: add(`function(prefix: value() + fn: name())
  : withParameters(fn: parameterNames())
  : returns(plus(
      constant(prefix: value() + " "),
      call(fn): withArgs(fn: parametersAsRefs()))))
  return fn
}

----
Decoration of struct that adds a `newStructname` function to the module
----
macro decostructfun = |s| {
  s: enclosingModule()?: addFunction(
    `function("new" + s: getName()): withParameters("a", "b")
      : returns(call(s: getName()): withArgs(
        plus(refLookup("a"), refLookup("b")),
        tuple(refLookup("b"), refLookup("a")))))
  return s
}

----
Decoration of struct that adds a `n` member to the struct
----
macro decostructmember = |n, s| {
  return s: withMember(n: value())
}

macro decostruct = |s| -> decostructmember(constant("args"), decostructfun(s))


macro withTest = |augm| {
  augm: addFunction(
    `function("is" + augm: name())
      : withParameters("this")
      : returns(constant(true)))
  augm: enclosingModule()?: add(
    `augment("java.lang.Object"): `with(
      `function("is" + augm: name())
        : withParameters("this")
        : returns(constant(false))))
  return augm
}

local function _withConstant = |val, augm| {
  augm: addFunction(
    `function(val: value()): withParameters("this"): returns(val))
  return augm
}

macro withConstant = |val, toplevel| {
  if toplevel oftype FunctionContainer.class {
    return _withConstant(val, toplevel)
  }
  toplevel: enclosingModule()?: add(
    _withConstant(val, `augment(toplevel: packageAndClass())))
  return toplevel
}


macro decoOverloaded = |fn| -> decoOverloaded(constant("default"), fn)

macro decoOverloaded = |msg, fn| -> fn: sameSignature()
  : returns(
    plus(
      constant(msg: value() + " "),
      lambda(): withParameters(fn: parameterNames())
      : block(fn: getBlock())
      : call(fn: parametersAsRefs())))

----
Create a polymorphic constructor for this union
----
macro decounion = |u| {
  let m = `match(): `otherwise(call("Predefined.raise"): withArgs(constant("invalid number or arguments")))
  foreach val in u: values() {
    let a = val: getMembers(): size()
    let cons = call(u: name() + "." + val: name())
    for (var i = 0, i < a, i = i + 1) {
      cons: withArgs(invoke("get"): withArgs(constant(i)): on(refLookup("args")))
    }
    m: `when(equals(invoke("length"): on(refLookup("args")), constant(a)))
     : `then(cons)
  }
  u: enclosingModule()?: addFunction(
    `function(u: getName()): withParameters("args"): varargs(true)
      : returns(m))
  return u
}


macro moduleMacro = |mod| {
  mod: addFunction(`function("generatedByModuleMacro")
    : returns(constant("module macro result")))
}
