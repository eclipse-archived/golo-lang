module PathologicPolymorphic

import java.lang
import java.util

function run = {
  let data = Array("foo", 666, Object(), "bar", 999, LinkedList(), HashMap(), TreeSet(), RuntimeException(),
                   IllegalArgumentException(), IllegalStateException(), Object(), Exception())
  let length = alength(data)
  var result = null
  for (var i = 0, i < 200000, i = i + 1) {
    for (var j = 0, j < length, j = j + 1) {
      result = aget(data, j): toString()
    }
  }
  return result
}

function run_with_elvis_and_nulls = {
  let data = Array(null, "foo", 666, Object(), "bar", 999, null, LinkedList(), HashMap(), TreeSet(), RuntimeException(),
                   IllegalArgumentException(), IllegalStateException())
  let length = alength(data)
  var result = null
  for (var i = 0, i < 200000, i = i + 1) {
    for (var j = 0, j < length, j = j + 1) {
      result = aget(data, j) ?: toString()
    }
  }
  return result
}