
module golotest.execution.Comprehension

function test_forloop = {
  let l = list[ 2 * x for (var x = 1, x < 5, x = x + 1) ]
  require(l == list[2, 4, 6, 8], "forloop failed")
}

function test_foreachloop = {
  let l = list[ 2 * x foreach x in [1..5] ]
  require(l == list[2, 4, 6, 8], "foreachloop failed")
}

function test_foreach_onref = {
  let a = [1..5]
  let l = list[ 2 * x foreach x in a ]
  require(l == list[2, 4, 6, 8], "foreach_onref failed")
}

function test_foreach_when = {
  let l = list[ x / 2 foreach x in [1..10] when (x % 2) == 0 ]
  require(l == list[1, 2, 3, 4], "foreach_when failed")
}

function test_vector = {
  let v = vector[ 2 * x foreach x in [1..5] ]
  require(v == vector[2, 4, 6, 8], "vector failed")
}

function test_array = {
  let v = array[ 2 * x foreach x in [1..5] ]
  require(java.util.Arrays.equals(v, array[2, 4, 6, 8]), "array failed")
}

function test_tuple = {
  let v = tuple[ 2 * x foreach x in [1..5] ]
  let t = [ 2 * x foreach x in [1..5] ]
  require(v == tuple[2, 4, 6, 8], "tuple failed")
  require(t == [2, 4, 6, 8], "tuple failed")
}

function test_set = {
  let l = list[1, 2, 3, 1, 2, 3, 1, 2, 3]
  let s = set[ x * 2 foreach x in l ]
  require(s == set[2, 4, 6], "set failed")
}

function test_map = {
  let target = map[
    ["k1", 1],
    ["k2", 2],
    ["k3", 3],
    ["k4", 4]
  ]
  let m1 = map[ mapEntry("k" + i, i) foreach i in [1..5] ]
  let m2 = map[ ["k" + i, i] foreach i in [1..5] ]
  require(m1 == target, "map on entry failed")
  require(m2 == target, "map on tuple failed")
}

function test_two_loops = {
  let l = list[ [x, y] foreach x in [0..3] foreach y in [0..3] ]
  require( l == list[
    [0, 0],
    [0, 1],
    [0, 2],
    [1, 0],
    [1, 1],
    [1, 2],
    [2, 0],
    [2, 1],
    [2, 2]
  ], "two_loops failed")
}

function test_two_mixed_loops = {
  let ref= list[
    [0, 0],
    [0, 1],
    [0, 2],
    [1, 0],
    [1, 1],
    [1, 2],
    [2, 0],
    [2, 1],
    [2, 2]
  ]
  let l1 = list[ [x, y] foreach x in [0..3] for (var y = 0, y < 3, y = y + 1) ]
  require(l1 == ref, "two_mixed_loops failed")
  let l2 = list[ [x, y] for (var x = 0, x < 3, x = x + 1) foreach y in [0..3] ]
  require(l2 == ref, "two_mixed_loops failed")
}

function test_more_loops = {
  let l = list[ [x,y,z] foreach x in [0,1,2] when (x % 2) == 1
                        foreach y in [1,2,3] when y < 2
                        foreach z in [1] ]
  require(l == list[[1,1,1]], "more_loops failed")
}

function test_destruct = {
  let data = [
    [1, 2],
    [2, 3],
    [3, 4]
  ]
  let l = list[ x + y foreach x, y in data ]
  require(l == list[3, 5, 7], "destruct failed")
}

function test_filtered_destruct_two = {
  let aMap = map[
    ["foo", 5],
    ["bar", 45],
    ["plop", 10]
  ]
  let l = list[
    (k + ": " + (x * v))
    foreach x in [1..10] when (x % 2) == 1
    foreach k, v in aMap:entrySet() when k: startsWith("f") or v >= 42
  ]

  let result = list[
    "foo: 5", "bar: 45",
    "foo: 15", "bar: 135",
    "foo: 25", "bar: 225",
    "foo: 35", "bar: 315",
    "foo: 45", "bar: 405"
  ]
  require(l == result, "filtered destruct failed")
}

function test_map_destruct = {
  let base = map[
    ["a", 1],
    ["b", 2],
    ["c", 3]
  ]
  let m = map[ [k, v] foreach k, v in base: entrySet() ]
  require(m == base, "map_destruct failed")
}

#........................................................

function run = |tests...| {
  var errorsnb = 0
  var testsnb = 0
  foreach t in tests {
    testsnb = testsnb + 1
    try {
      t()
    } catch (e) {
      errorsnb = errorsnb + 1
      println("E: " + e: getMessage())
    }
  }
  println("Ran " + testsnb + " tests, " + errorsnb + " errors")
  if (errorsnb == 0) {
    println("OK")
    System.exit(0)
  } else {
    println("FAILED")
    System.exit(1)
  }

}
function main = |args| {
  run(
    ^test_forloop,
    ^test_foreachloop,
    ^test_foreach_onref,
    ^test_foreach_when,
    ^test_vector,
    ^test_set,
    ^test_map,
    ^test_array,
    ^test_tuple,
    ^test_two_loops,
    ^test_two_mixed_loops,
    ^test_more_loops,
    ^test_destruct,
    ^test_map_destruct,
    ^test_filtered_destruct_two
  )
}
