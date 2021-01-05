# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module DealingWithNull

import java.util

function main = |args| {

  # Data model
  let contacts = map[
    ["mrbean", map[
      ["email", "bean@gmail.com"],
      ["url", "http://mrbean.com"]
    ]],
    ["larry", map[
      ["email", "larry@iamricherthanyou.com"]
    ]]
  ]

  # MrBean and Larry
  let mrbean = contacts: get("mrbean")
  let larry = contacts: get("larry")

  # Illustrates orIfNull
  println(mrbean: get("url") orIfNull "n/a")
  println(larry: get("url") orIfNull "n/a")

  # Querying a non-existent data model because there is no 'address' entry
  println(mrbean: get("address")?: street()?: number() orIfNull "n/a")
}
