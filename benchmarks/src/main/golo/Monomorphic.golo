module Monomorphic

function run = {
  var result = null
  for (var i = 0, i < 5000000, i = i + 1) {
    result = i: toString()
  }
  return result
}

function run_elvis = {
  var result = null
  for (var i = 0, i < 5000000, i = i + 1) {
    result = i?: toString()
  }
  return result
}

function run_elvis_with_nulls = {
  var result = null
  var obj = null
  let random = java.util.Random()
  for (var i = 0, i < 5000000, i = i + 1) {
    if random: nextBoolean() {
      obj = null
    } else {
      obj = i
    }
    result = obj?: toString()
  }
  return result
}

