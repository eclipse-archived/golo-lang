#!/usr/bin/env golosh
# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module hello

function main = |args| {
  require(args: length() > 1, "You should set at least one argument!")
  println("Hello " + args: get(1) + " from '" + args: get(0) + "'!")
}
