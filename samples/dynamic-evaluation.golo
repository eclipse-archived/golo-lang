# Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

module samples.DynamicEvaluation

import gololang.EvaluationEnvironment

local function test_fullModule = |env| {
  let code =
"""
module foo

function a = -> "a!"
function b = -> "b!"
"""
  let mod = env: fullModule(code)
  let a = fun("a", mod)
  let b = fun("b", mod)
  println(">>> fullModule()")
  println(a())
  println(b())
}

local function test_anonymousModule = |env| {
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
}

local function test_func = |env| {
  let code = "return (a + b) * 2"
  let f = env: func(code, "a", "b")
  println(">>> func")
  println(f(10, 20))
}

local function test_defunc = |env| {
  let code = "|a, b| -> (a + b) * 2"
  let f = env: defunc(code)
  println(">>> defunc")
  println(f(10, 20))
}

local function test_run = |env| {
  let code = """println(">>> run")
  foreach (i in range(0, 3)) {
    println("w00t")
  }"""
  env: run(code)
}

local function test_run_map = |env| {
  let code = """println(">>> run_map")
  println(a)
  println(b)
  """
  let values = java.util.TreeMap(): add("a", 1): add("b", 2)
  env: run(code, values)
}

function main = |args| {
  let env = EvaluationEnvironment()
  test_fullModule(env)
  test_anonymousModule(env)
  test_func(env)
  test_defunc(env)
  test_run(env)
  test_run_map(env)
}
