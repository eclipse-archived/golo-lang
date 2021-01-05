# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.DynamicObjectPerson

#tag::sample[]
local function mrbean = -> DynamicObject():
  name("Mr Bean"):
  email("mrbean@gmail.com"):
  define("toString", |this| -> this: name() + " <" + this: email() + ">")

function main = |args| {

  let bean = mrbean()
  println(bean: toString())

  bean: email("mrbean@outlook.com")
  println(bean: toString())
}
#end::sample[]
