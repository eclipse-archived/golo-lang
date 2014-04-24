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

module gololang.JSON

# ............................................................................................... #

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
    return org.json.simple.JSONObject(obj)
  } else if isSeq(obj) {
    let json = org.json.simple.JSONArray()
    foreach value in obj {
      json: add(value)
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
      json: put(member, obj: get(member))
    }
    return json
  }
  return obj
}

function parse = |str| -> org.json.simple.JSONValue.parseWithException(str)

# ............................................................................................... #

function dynamicObjectMixin = ->
  DynamicObject(): define("toJSON", |this| -> stringify(this))

function toDynamicObject = |str| {
  let obj = DynamicObject()
  let map = parse(str)
  foreach key in map: keySet() {
    obj: define(key, map: get(key))
  }
  return obj
}

# ............................................................................................... #

augment gololang.GoloStruct {

  function toJSON = |this| -> stringify(this)

  function updateFromJSON = |this, str| {
    let map = parse(str)
    foreach member in this: members() {
      this: set(member, map: get(member))
    }
    return this
  }
}

# ............................................................................................... #
