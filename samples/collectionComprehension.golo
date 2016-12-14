# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php
module samples.CollectionComprehension

struct Duck = {name, age}

struct Person = {id, name, age}
struct Command = {customer, product}

function main = |args| {

  let aList = list[
    Duck("D1", 3),
    Duck("D2", 2),
    Duck("D3", 6),
    Duck("D4", 4)
  ]

  # similar to filter + map
  let messages = list[
    "%s is %d years old": format(d: name(), d:age())
    foreach d in aList
    when d: age() < 5
  ]
  println(messages)

  # similar to nested loops. Works with any iterable.
  # also change the collection type
  let twoDicesValues = set[x + y foreach x in [1..7] foreach y in [1..7]]
  println(twoDicesValues)

  # `for` loop
  println(list[2 * x for (var x=0, x < 10, x = x + 1)])

  # destructuring
  let couples = [ [1, 2], [2, 3], [3, 4] ]
  println(list[ a + b foreach a, b in couples ])


  sqllike()

}

----
tag::sql[]
  select distinct
    p.name, p.age, c.product
  from
    persons as p,
    commands as c
  where
    p.id == c.customer
    and p.age > 18
end::sql[]
----
local function sqllike = {
  # SQL like
  let persons = list[
    Person(1, "Foo", 14),
    Person(2, "Bar", 25)
  ]

  let commands = list[
    Command(1, "prod1"),
    Command(1, "prod2"),
    Command(1, "prod3"),
    Command(2, "prod4"),
    Command(2, "prod1"),
    Command(2, "prod4"),
    Command(2, "prod3"),
    Command(2, "prod3"),
    Command(2, "prod4")
  ]

#tag::sqlLike[]
  println(set[
    [p: name(), p: age(), c: product()]
    foreach p in persons
    foreach c in commands
    when p: id() == c: customer()
         and p: age() > 18
  ])
#end::sqlLike[]
}
