# Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.Unions

union Option = {
  Some = { value }
  None
}

augment Option {
  function flatMap = |this, func| -> match {
    when this is None() then this
    otherwise func(this: value())
  }

  function map = |this, func| -> match {
    when this is None() then this
    otherwise Some(func(this: value()))
  }
}

function monadicAdd = |mx, my| ->
  mx: flatMap(|x| ->
    my: flatMap(|y| ->
      Some(x + y)))

union Tree = {
  Node = {left, right}
  Leaf = {value}
  Empty
}

augment Tree {
  function whoAreYou = |this| -> match {
    when this oftype Tree$Node.class then "I'm a node"
    when this oftype Tree$Leaf.class then "I'm a leaf"
    otherwise "I'm empty"
  }
}

let _ = Unknown.get()

function in_a_match = |tree| -> match {
  when tree: isEmpty() then "empty tree"
  when tree: isLeaf(0) then "a leaf with 0"
  when tree: isLeaf() then "a leaf"
  when tree: isNode(Empty(), _) or tree: isNode(_, Empty()) then "node with 1 child"
  when tree: isNode() then "a node"
  otherwise "wtf"
}

function test_match = {
  require(in_a_match(Empty()) == "empty tree", "err")
  require(in_a_match(Leaf(0)) == "a leaf with 0", "err")
  require(in_a_match(Leaf(42)) == "a leaf", "err")
  require(in_a_match(Node(0, 0)) == "a node", "err")
  require(in_a_match(Node(Tree.Empty(), 0)) == "node with 1 child", "err")
  require(in_a_match(Node(0, Tree.Empty())) == "node with 1 child", "err")
}

function test_option = {
  let n = None()
  let s = Some(5)

  println(n)
  println(s)

  require(n == None(), "err")
  require(n is None(), "err")

  require(s == Some(5), "err")
  require(s isnt Some(5), "err")

  require(monadicAdd(n, n) == n, "err")
  require(monadicAdd(s, n) == n, "err")
  require(monadicAdd(n, s) == n, "err")
  require(monadicAdd(s, s) == Some(10), "err")

  let double = |x| -> 2 * x
  require(n: map(double) == n, "err")
  require(s: map(double) == Some(10), "err")

}

function test_tree = {
  println(Node(Empty(), Leaf(0)))

  require(not Leaf(0): isEmpty(), "err on Tree.Leaf:isEmpty")
  require(not Node(0, 0): isEmpty(), "err on Tree.Node:isEmpty")
  require(Empty(): isEmpty(), "err on Tree.Empty:isEmpty")

  require(Leaf(0): whoAreYou() == "I'm a leaf",
    "err on Tree.Leaf:whoAreYou")
  require(Node(0, 0): whoAreYou() == "I'm a node",
    "err on Tree.Node:whoAreYou")
  require(Empty(): whoAreYou() == "I'm empty",
    "err on Tree.Empty:whoAreYou")
}

function main = |args| {
  test_option()
  test_tree()
  test_match()

  println("OK")
}
