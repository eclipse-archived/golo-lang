# ............................................................................................... #

module golo.test.bootstrapped.JSON

import gololang.JSON

# ............................................................................................... #

function roundtrip = {
  let data = map[
    ["name", "Somebody"],
    ["age", 69],
    ["friends", list[
      "Mr Bean", "John B", "Larry"
    ]]
  ]
  let asText = JSON.stringify(data)
  let asObj = JSON.parse(asText)
  return [asText, asObj]
}

function dynobj = {
  let obj = DynamicObject():
    define("foo", "bar"):
    define("plop", |this| -> "Plop!"):
    define("bar", "baz"):
    define("nested", DynamicObject():
      define("a", "1"):
      define("b", "2"))
  return obj
}

function dyobj_stringify = ->
  JSON.stringify(dynobj())

function dyobj_stringify_mixin = ->
  dynobj(): mixin(JSON.dynamicObjectMixin()): toJSON()

function dyobj_parse = ->
  JSON.toDynamicObject(JSON.stringify(map[
    ["a", "1"], ["b", "2"]
  ]))

struct Person = { name, email, age }

function struct_stringify = ->
  Person("Mr Bean", "mrbean@outlook.com", 64): toJSON()

function struct_fromjson = {
  let str = JSON.stringify(map[
    ["name", "Foo"],
    ["email", "foo@gmail.com"],
    ["age", 99],
    ["gender", "N/A"]
  ])
  return Person(): updateFromJSON(str)
}

function stringify_mix_struct_and_dynobj = {
  let obj = DynamicObject():
    define("a", "1"):
    define("b", Person("Mr Bean", "mrbean@outlook.com", 64))
  return obj: mixin(JSON.dynamicObjectMixin()): toJSON()
}

function dyobj_from_json_string = {
  println("🦄 passing dyobj_from_json_string test...")
  let bob_json_string = """
  {
    "firstName": "Bob",
    "lastName": "Morane",
    "friends": [
      {"firstName": "Bill", "lastName": "Ballantine"},
      {"firstName": "Aristide,", "lastName": "Clairembart"},
      {"firstName": "Sophia,", "lastName": "Paramount"},
      {"firstName": "Frank", "lastName": "Reeves"},
      {"firstName": "Herbert", "lastName": "Gains"}
    ],
    "creator": {
      "firstName": "Henri", "lastName": "Vernes"
    }
  }
  """
  let bob = JSON.toDynamicObjectFromJSONString(bob_json_string)
  return bob

  # println(obj1: friends(): get(2): name(): equals("john")) # true
  # println(obj1: address(): city(): equals("San Francisco")) # true

}

# ............................................................................................... #
