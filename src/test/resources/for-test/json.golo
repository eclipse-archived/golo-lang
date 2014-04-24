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

# ............................................................................................... #
