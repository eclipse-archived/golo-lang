# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module sample.EnumsThreadState

import java.lang.Thread$State

function main = |args| {

  # Call the enum entry like a function
  let new = Thread$State.NEW()
  println("name=" + new: name() + ", ordinal=" + new: ordinal())
  println("-----------")

  # Walk through all enum entries
  foreach element in Thread$State.values() {
    println("name=" + element: name() + ", ordinal=" + element: ordinal())
  }
}
