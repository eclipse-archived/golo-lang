module CollectionLiterals

function tuple = -> [1, 2, 3, ["a", "b"]]

function funky_data = -> map[
  ["foo", "bar"],
  ["plop", set[1, 2, 3, 4, 5]],
  ["mrbean", map[
    ["name", "Mr Bean"],
    ["email", "bean@outlook.com"]
  ]]
]
