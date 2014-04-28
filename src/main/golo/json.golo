# ............................................................................................... #
#
# Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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
