# Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

module samples.Unions

union Option = {
  Some = { value }
  None
}

augment Option {
  function flatMap = |this, func| -> match {
    when this is Option.None() then this
    otherwise func(this: value())
  }

  function map = |this, func| -> match {
    when this is Option.None() then this
    otherwise Option.Some(func(this: value()))
  }
}

function monadicAdd = |mx, my| -> 
  mx: flatMap(|x| ->
    my: flatMap(|y| ->
      Option.Some(x + y)))

union Tree = {
  Node = {left, right}
  Leaf = {value}
  Empty
}

augment Tree$Node {
  function isEmpty = |this| -> false
}

augment Tree$Empty {
  function isEmpty = |this| -> true
}

augment Tree$Leaf {
  function isEmpty = |this| -> false
}

augment Tree {
  function whoAreYou = |this| -> match {
    when this oftype samples.Unions.types.Tree$Node.class then "I'm a node"
    when this oftype samples.Unions.types.Tree$Leaf.class then "I'm a leaf"
    otherwise "I'm empty"
  }
}

function test_option = {
  let n = Option.None()
  let s = Option.Some(5)

  println(n)
  println(s)
  
  require(n == Option.None(), "err")
  require(n is Option.None(), "err")
  
  require(s == Option.Some(5), "err")
  require(s isnt Option.Some(5), "err")

  require(monadicAdd(n, n) == n, "err")
  require(monadicAdd(s, n) == n, "err")
  require(monadicAdd(n, s) == n, "err")
  require(monadicAdd(s, s) == Option.Some(10), "err")

  let double = |x| -> 2 * x
  require(n: map(double) == n, "err")
  require(s: map(double) == Option.Some(10), "err")

}

function test_tree = {
  println(Tree.Node(Tree.Empty(), Tree.Leaf(0)))

  require(not Tree.Leaf(0): isEmpty(), "err on Tree.Leaf:isEmpty")
  require(not Tree.Node(0, 0): isEmpty(), "err on Tree.Node:isEmpty")
  require(Tree.Empty(): isEmpty(), "err on Tree.Empty:isEmpty")

  require(Tree.Leaf(0): whoAreYou() == "I'm a leaf", 
    "err on Tree.Leaf:whoAreYou")
  require(Tree.Node(0, 0): whoAreYou() == "I'm a node",
    "err on Tree.Node:whoAreYou")
  require(Tree.Empty(): whoAreYou() == "I'm empty", 
    "err on Tree.Empty:whoAreYou")
}

function main = |args| {
  test_option()
  test_tree()

  println("OK")
}
