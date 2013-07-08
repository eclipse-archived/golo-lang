module golotest.execution.CollectionLiterals

function nested_tuples = -> [1, 2, 3, [10, 20]]

function some_array = -> array[1, "a"]

function some_list = -> list[1, 2, 3]

function some_vector = -> vector[1, 2, 3]

function some_set = -> set["a", "b", "a"]

function some_map = -> map[["foo", "bar"], ["plop", "da plop"]]

function empty_tuple = -> []
