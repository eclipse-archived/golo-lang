module TriMorphic

import java.lang

function run = {
  let data = Array("foo", 666, "bar", 999, "plop", "da", "plop", "for", "ever",
                   1, 2, 3, 4, 5, 6, Object(), Object(), Object(), Object())
  let length = alength(data)
  var result = null
  for (var i = 0, i < 200000, i = i + 1) {
    for (var j = 0, j < length, j = j + 1) {
      result = aget(data, j): toString()
    }
  }
  return result
}