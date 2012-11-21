module Polymorphic

import java.lang
import java.util

function run = {
  let data = Array("foo", 666, Object(), "bar", 999, LinkedList(), HashMap(), TreeSet(), RuntimeException(),
                   IllegalArgumentException(), IllegalStateException(), Object(), Exception())
  let length = alength(data)
  var result = null
  for (var i = 0, i < 1000000, i = i + 1) {
    for (var j = 0, j < length, j = j + 1) {
      result = aget(data, j): toString()
    }
  }
  return result
}