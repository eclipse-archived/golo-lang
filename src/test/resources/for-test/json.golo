# ............................................................................................... #

module golo.test.bootstrapped.JSON

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
  JSON.dynamicObjectToJSON(dynobj())

function dyobj_stringify_mixin = ->
  dynobj(): mixin(JSON.dynamicObjectMixin()): toJSON()

function dyobj_parse = ->
  JSON.toDynamicObject(JSON.stringify(map[
    ["a", "1"], ["b", "2"]
  ]))

# ............................................................................................... #
