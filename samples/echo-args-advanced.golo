# Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module EchoArgsAdvanced

function main = |args| {

  println("With a for loop and an index:")
  for (var i = 0, i < args: length(), i = i + 1) {
    println("  #" + i + " -> " + args: get(i))
  }

  println("With a foreach loop:")
  foreach arg in args {
    println("  " + arg)
  }

  println("With a foreach over a range:")
  foreach i in range(0, args: length()) {
    println("  #" + i + " -> " + args: get(i))
  }

  println("With a foreach and a guard to keep arguments with at least 3 characters:")
  foreach arg in args when arg: length() > 2 {
    println("  " + arg)
  }
}
