module golotest.execution.CollectionLiterals

function nested_tuples = -> [1, 2, 3, [10, 20]]

function some_array = -> array[1, "a"]

function some_list = -> list[1, 2, 3]

function some_vector = -> vector[1, 2, 3]

function some_set = -> set["a", "b", "a"]

function some_map = -> map[["foo", "bar"], ["plop", "da plop"]]

function empty_tuple = -> []

function int_range = -> [0..10]

function char_range = -> ['a'..'f']

function with_expressions = {
#tag::with_expressions[]
  let a = -> [1, 'a']
  let b = -> [2, 'b']
  let m = map[a(), b()]
#end::with_expressions[]
  return m
}
