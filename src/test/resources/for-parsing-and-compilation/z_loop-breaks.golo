module breaking.loops

function a = {
  foreach (i in range(0, 100)) {
    if i == 50 {
      break
    }
  }
}

function b = {
  foreach (i in range(0, 100)) {
    if i < 10 {
      continue
    } else {
      break
    }
  }
}
