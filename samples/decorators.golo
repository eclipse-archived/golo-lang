# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.Decorators

import java.util.LinkedList

function simple_decorator = |func| {
  return |a,b| -> func(a+1,b+1)
}

@simple_decorator
function simple_adder = |x,y| -> x + y

function decorator_with_params = |param1, param2|{
  return |func| {
    return |a,b| -> func(a+param1,b+param2)
  }
}

@decorator_with_params(10,2)
function parametrized_adder = |x,y| -> x + y

function generic_decorator = |func| {
  return |args...| {
    println("number of params : "+args: length())
    return func: invoke(args)
  }
}

@generic_decorator
function generic_adder0 = -> 42

@generic_decorator
function generic_adder1 = |x| -> x

@generic_decorator
function generic_adder2 = |x,y| -> x + y

@generic_decorator
function generic_adder3 = |x,y,z| -> x + y + z

function list_sum_decorator = |func| {
    return |this| -> func(this) - 8
}

augment java.util.List {

  @list_sum_decorator
  function sum = |this| {
    var acc = 0
    foreach elem in this {
        acc = acc + elem
    }
    return acc
  }
}

function main = |args| {
  println(simple_adder(10,30))
  println(parametrized_adder(10,20))
  println(generic_adder0())
  println(generic_adder1(42))
  println(generic_adder2(20,22))
  println(generic_adder3(10,12,20))
  let list = LinkedList()
  list: add(5)
  list: add(10)
  list: add(15)
  list: add(20)
  println(list: sum())
}
