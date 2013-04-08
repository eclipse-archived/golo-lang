module golotest.execution.ContinueAndBreak

function twenty_four = {
  var result = 0
  foreach (i in range(0, 100)) {
    foreach (j in range(0, 100)) {
      if (i == 10) and (j == 5) {
        result = j
        break
      }
    }
    if i == 10 {
      result = result + i
      break
    }
  }
  foreach (k in range(0, 10)) {
    if k < 9 {
      continue
    }
    result = result + k
  }
  return result
}
