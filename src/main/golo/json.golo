# ............................................................................................... #
#
# Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# ............................................................................................... #

----
A set of useful APIs for dealing with JSON documents from Golo.

The implementation is backed by [json-simple](https://code.google.com/p/json-simple/). While
`json-simple` only supports encoding from lists and maps, this API brings support for sets, arrays,
Golo tuples, dynamic objects and structs.
----
module gololang.JSON

# ............................................................................................... #

----
Takes any know object, and gives a JSON string representation:

    let data = map[
      ["name", "Somebody"],
      ["age", 69],
      ["friends", list[
        "Mr Bean", "John B", "Larry"
      ]]
    ]
    let asText = JSON.stringify(data)

`obj` may be a list, an array, a set, a map, a tuple, a dynamic object or a struct. If `obj` is from
another type then its string representation is given according to `obj: toString()` or `"null"` if
`obj` is `null`.
----
function stringify = |obj| {
  let res = stringify_walk(obj)
  if (res is null) {
    return "null"
  } else {
    return res: toString()
  }
}

local function isSeq = |obj| ->
  (obj oftype java.util.List.class) or
  (obj oftype java.util.Set.class) or
  (obj oftype gololang.Tuple.class) or
  (isArray(obj))

local function stringify_walk = |obj| {
  if obj oftype java.util.Map.class {
    let json = org.json.simple.JSONObject()
    foreach key in obj: keySet() {
      json: put(key, stringify_walk(obj: get(key)))
    }
    return json
  } else if isSeq(obj) {
    let json = org.json.simple.JSONArray()
    foreach value in obj {
      json: add(stringify_walk(value))
    }
    return json
  } else if obj oftype gololang.DynamicObject.class {
    let json = org.json.simple.JSONObject()
    foreach prop in obj: properties() {
      let value = prop: getValue()
      if not(isClosure(value)) {
        json: put(prop: getKey(), stringify_walk(value))
      }
    }
    return json
  } else if (obj oftype gololang.GoloStruct.class) {
    let json = org.json.simple.JSONObject()
    foreach member in obj: members() {
      json: put(member, stringify_walk(obj: get(member)))
    }
    return json
  }
  return obj
}

----
Parses a JSON string and gives an object representation as a list or map collection:

    let data = JSON.parse(text)
    println(data: get("name"))
----
function parse = |str| -> org.json.simple.JSONValue.parseWithException(str)

# ............................................................................................... #

----
Provides a mixin for dynamic objects that includes a `toJSON()` method:

    object: mixin(JSON.dynamicObjectMixin()): toJSON()

which is equivalent to:

    JSON.stringify(object)
----
function dynamicObjectMixin = ->
  DynamicObject(): define("toJSON", |this| -> stringify(this))

----
Returns a new dynamic object from a JSON string where each first-level entry is mapped into the
dynamic object:

    let obj = JSON.toDynamicObject(JSON.stringify(map[
      ["a", "1"], ["b", "2"]
    ]))

    println(obj: a())
    println(obj: b())
----
function toDynamicObject = |str| {
  let obj = DynamicObject()
  let map = parse(str)
  foreach key in map: keySet() {
    obj: define(key, map: get(key))
  }
  return obj
}

----
Returns a new dynamic object from a JSONObject where each level entry is mapped into the
dynamic object or an array of dynamic objects.
----
local function toDynamicObjectFromJSONObject = |obj| {
  let isJSONObject = |obj| -> obj oftype org.json.simple.JSONObject.class
  let isJSONArray = |obj| -> obj oftype org.json.simple.JSONArray.class

  let parse_object = |level, obj, dyno| {
    let parseMembers = |obj, dyno| {
      obj: each(|key, value| {
        dyno: define(key, value)
        parse_object(key, value, dyno)
      })
    }

    if isJSONObject(obj) {
      if level is null { # root
        parseMembers(obj, dyno)
      } else {
        dyno: define(level, DynamicObject())
        parseMembers(obj, dyno: get(level))
      }
    } else if isJSONArray(obj) {
        dyno: define(level, list[])
        obj: each(|item| {
          if isJSONObject(item) is false and isJSONArray(item) is false {
            dyno: get(level): append(item)
          } else if isJSONObject(item) {
              let subDyno = DynamicObject()
              parseMembers(item, subDyno)
              dyno: get(level): append(subDyno)
          }
        })
    }
    return dyno
  }
  return parse_object(null, obj, DynamicObject())
}

----
Returns a list of dynamic objects from a JSONArray where each level entry is mapped into the
dynamic object or an array of dynamic objects.
----
local function toDynamicObjectsListFromJSONArray = |arr| -> arr: map(|obj| -> toDynamicObjectFromJSONObject(obj))

----
Returns a new dynamic object from a JSON string where each level entry is mapped into the
dynamic object or an array of dynamic objects:

    let obj = JSON.toDynamicObjectFromJSONString("""
      {
        "id":"bob",
        "friends":[
          {"name":"sam"}, {"name":"jane"}, {"name":"john"}
        ],
        "address": {
          "street":"20 Avenue Albert Einstein",
          "city":"Villeurbanne",
          "zip":"69100",
          "country":"France"
        }
      }
    """)

    obj: friends(): get(2): name(): equals("john") # true
    obj: address(): city(): equals("Villeurbanne") # true
----
function toDynamicObjectFromJSONString = |str| -> toDynamicObjectFromJSONObject(parse(str))

----
Returns a list of dynamic objects from a JSON string where each level entry is mapped into the
dynamic object or an array of dynamic objects:

    let objects = JSON.toDynamicObjectsListFromJSONString("""[
      {"message":"hello"},
      {
        "id":"bob",
        "friends":[
          {"name":"sam"}, {"name":"jane"}, {"name":"john"}
        ],
        "address": {
          "street":"20 Avenue Albert Einstein",
          "city":"Villeurbanne",
          "zip":"69100",
          "country":"France"
        }
      }
    ]""")

    println(objects: get(1): friends(): get(2): name(): equals("john")) # true
    println(objects: get(1): address(): city(): equals("Villeurbanne")) # true
----
function toDynamicObjectsListFromJSONString = |str| -> toDynamicObjectsListFromJSONArray(parse(str))

----
Returns a new dynamic object from a map where each level entry is mapped into the
dynamic object or an array of dynamic objects:

    let obj = JSON.toDynamicObjectFromMap(map[
      ["id", "bob"],
      ["friends", [
        map[["name", "sam"]],
        map[["name", "jane"]],
        map[["name", "john"]]
      ]],
      ["address", map[
        ["street", "20 Avenue Albert Einstein"],
        ["city", "Villeurbanne"],
        ["zip", "69100"],
        ["country", "France"]
      ]]
    ])

    println(obj: friends(): get(2): name(): equals("john")) # true
    println(obj: address(): city(): equals("Villeurbanne")) # true
----
function toDynamicObjectFromMap = |mapInstance| -> toDynamicObjectFromJSONObject(parse(stringify(mapInstance)))

----
Returns a list of dynamic objects from a collection of maps where each level entry is mapped into the
dynamic object or an array of dynamic objects:

    let objects = JSON.toDynamicObjectsListFromMapsCollection([
      map[["message", "hello"]],
      map[
        ["id", "bob"],
        ["friends", list[
          map[["name", "sam"]],
          map[["name", "jane"]],
          map[["name", "john"]]
        ]],
        ["address", map[
          ["street", "20 Avenue Albert Einstein"],
          ["city", "Villeurbanne"],
          ["zip", "69100"],
          ["country", "France"]
        ]]
      ]
    ])

    println(objects: get(1): friends(): get(2): name(): equals("john")) # true
    println(objects: get(1): address(): city(): equals("Villeurbanne")) # true
----
function toDynamicObjectsListFromMapsCollection = |mapInstance| -> toDynamicObjectsListFromJSONArray(parse(stringify(mapInstance)))

# ............................................................................................... #

----
JSON augmentations for structs.
----
augment gololang.GoloStruct {

  ----
  Conveniently adds a `toJSON()` method, which is equivalent to calling `JSON.stringify()`:

      struct Person = { name, age, email }
      # (...)

      Person("Mr Bean", "mrbean@outlook.com", 64): toJSON()
  ----
  function toJSON = |this| -> stringify(this)

  ----
  Populates the elements of a struct based on the values found in a JSON string.

      let str = JSON.stringify(map[
        ["name", "Foo"],
        ["email", "foo@gmail.com"],
        ["age", 99],
        ["gender", "N/A"]
      ])
      let foo = Person(): updateFromJSON(str)

  Note that missing entries from the JSON data yield `null` values in the struct.
  ----
  function updateFromJSON = |this, str| {
    let map = parse(str)
    foreach member in this: members() {
      this: set(member, map: get(member))
    }
    return this
  }
}

# ............................................................................................... #
