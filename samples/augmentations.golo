# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.Augmentations

import java.util.LinkedList

augment java.util.List {
  function `with = |this, value| {
    this: add(value)
    return this
  }
}

augment java.util.Collection {
  function doToEach = |this, func| {
    foreach (element in this) {
      func(element)
    }
  }
}

function main = |args| {
  let list = LinkedList(): `with("foo"): `with("bar"): `with("baz")
  list: doToEach(|value| -> println(">>> " + value))
}
