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

function stringify = |obj| -> org.json.simple.JSONValue.toJSONString(obj)

function parse = |str| -> org.json.simple.JSONValue.parseWithException(str)

function dynamicObjectToJSON = |dynobj| {
  let json = org.json.simple.JSONObject()
  foreach prop in dynobj: properties() {
    let value = prop: getValue()
    if not(isClosure(value)) {
      let encodedValue = match {
        when value oftype gololang.DynamicObject.class then dynamicObjectToJSON(value)
        otherwise value
      }
      json: put(prop: getKey(), encodedValue)
    }
  }
  return json: toJSONString()
}

function dynamicObjectMixin = -> DynamicObject(): define("toJSON", ^dynamicObjectToJSON)

function toDynamicObject = |str| {
  let obj = DynamicObject()
  let map = parse(str)
  foreach key in map: keySet() {
    obj: define(key, map: get(key))
  }
  return obj
}
