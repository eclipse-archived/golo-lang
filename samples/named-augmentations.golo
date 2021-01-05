# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.NamedAugmentations

import java.util.LinkedList

----
Augmentations to deal with collections
----
augmentation EasyList = {
  ----
  apply a function to each element
  ----
  function doToEach = |this, func| {
    foreach (element in this) {
      func(element)
    }
  }
}

----
Augmentation to make an object plop
----
augmentation Plopable = {
  ----
  say plop
  ----
  function plop = |this| -> "plop"
}

----
Augmentation to make an object bar
----
augmentation Barator = {

  ----
  say bar
  ----
  function bar = |this| -> "bar"
}

augment java.util.Collection with Plopable, Barator


----
Augmentations on lists
----
augment java.util.List {

  ----
  say baz
  ----
  function baz = |this| -> "baz"
}

augment java.util.List with EasyList

struct MyStruct = {val}

augment MyStruct with Plopable

function main = |args| {
  let list = list["foo", "bar", "baz"]
  list: doToEach(|value| -> println(">>> " + value))
  println("are you a barator? " + list: bar())
  println("are you plopable? " + list: plop())
  println("are you plopable? " + MyStruct(1): plop())
  println("can you baz? " + list: baz())
}
