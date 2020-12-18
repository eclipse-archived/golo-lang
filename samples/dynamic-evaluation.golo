# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.DynamicEvaluation

import gololang.EvaluationEnvironment

local function test_asModule = |env| {
#tag::asModule[]
  let code =
"""
module foo

function a = -> "a!"
function b = -> "b!"
"""
  let mod = env: asModule(code)
  let a = fun("a", mod)
  let b = fun("b", mod)
  println(">>> asModule()")
  println(a())
  println(b())
#end::asModule[]
}

local function test_anonymousModule = |env| {
#tag::anonymousModule[]
  let code =
"""
function a = -> "a."
function b = -> "b."
"""
  let mod = env: anonymousModule(code)
  let a = fun("a", mod)
  let b = fun("b", mod)
  println(">>> anonymousModule()")
  println(a())
  println(b())
#end::anonymousModule[]
}

local function test_asFunction = |env| {
#tag::asFunction[]
  let code = "return (a + b) * 2"
  let f = env: asFunction(code, "a", "b")
  println(">>> asFunction")
  println(f(10, 20))
#end::asFunction[]
}

local function test_def = |env| {
#tag::def[]
  let code = "|a, b| -> (a + b) * 2"
  let f = env: def(code)
  println(">>> def")
  println(f(10, 20))
#tag::def[]
}

local function test_run = |env| {
#tag::run[]
  let code = """println(">>> run")
  foreach (i in range(0, 3)) {
    println("w00t")
  }
  return 666"""
  println(env: run(code)) # => "w00t"x3 and "666"
#end::run[]
}

local function test_run_map = |env| {
#tag::map[]
  let code = """println(">>> run_map")
  println(a)
  println(b)
  """
  let values = java.util.TreeMap(): add("a", 1): add("b", 2)
  env: run(code, values)
#end::map[]
}

function main = |args| {
  let env = EvaluationEnvironment()
  test_asModule(env)
  test_anonymousModule(env)
  test_asFunction(env)
  test_def(env)
  test_run(env)
  test_run_map(env)
}
