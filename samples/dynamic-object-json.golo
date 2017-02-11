# Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.DynamicObjectFromString

import gololang.JSON

function main = |args| {
  # cast a json string to a DynamicObject
  let obj1 = JSON.toDynamicObjectFromJSONString("""
    {
      "id":"bob",
      "remarks": null,
      "friends":[
        {"name":"sam"}, {"name":"jane"}, {"name":"john"}
      ],
      "address": {
        "street":"88 Colin P Kelly Jr St",
        "city":"San Francisco",
        "zip":"CA 94107",
        "country":"United States"
      }
    }
  """)

  println(obj1: friends(): get(2): name(): equals("john")) # true
  println(obj1: address(): city(): equals("San Francisco")) # true

  # cast a map to a DynamicObject
  let obj2 = JSON.toDynamicObjectFromMap(map[
    ["id", "bob"],
    ["friends", [
      map[["name", "sam"]],
      map[["name", "jane"]],
      map[["name", "john"]]
    ]],
    ["address", map[
      ["street", "88 Colin P Kelly Jr St"],
      ["city", "San Francisco"],
      ["zip", "CA 94107"],
      ["country", "United States"]
    ]]
  ])

  println(obj2: friends(): get(2): name(): equals("john")) # true
  println(obj2: address(): city(): equals("San Francisco")) # true

  # cast json string to a DynamicObjects list
  let objects1 = JSON.toDynamicObjectsListFromJSONString("""[
    {"message":"hello"},
    {
      "id":"bob",
      "friends":[
        {"name":"sam"}, {"name":"jane"}, {"name":"john"}
      ],
      "address": {
        "street":"88 Colin P Kelly Jr St",
        "city":"San Francisco",
        "zip":"CA 94107",
        "country":"United States"
      }
    }
  ]""")

  println(objects1: get(1): friends(): get(2): name(): equals("john")) # true
  println(objects1: get(1): address(): city(): equals("San Francisco")) # true

  # cast maps collection to a DynamicObjects list
  let objects2 = JSON.toDynamicObjectsListFromMapsCollection([
    map[["message", "hello"]],
    map[
      ["id", "bob"],
      ["friends", list[
        map[["name", "sam"]],
        map[["name", "jane"]],
        map[["name", "john"]]
      ]],
      ["address", map[
        ["street", "88 Colin P Kelly Jr St"],
        ["city", "San Francisco"],
        ["zip", "CA 94107"],
        ["country", "United States"]
      ]]
    ]
  ])

  println(objects2: get(1): friends(): get(2): name(): equals("john")) # true
  println(objects2: get(1): address(): city(): equals("San Francisco")) # true
}
