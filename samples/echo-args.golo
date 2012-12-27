module EchoArgs

function main = |args| {
  
  println("With a for loop and an index:")
  for (var i = 0, i < alength(args), i = i + 1) {
    println("  #" + i + " -> " + aget(args, i))
  }

  println("With a foreach loop:")
  foreach (arg in atoList(args)) {
    println("  " + arg)
  }

  println("With a foreach over a range:")
  foreach (i in range(0, alength(args) - 1)) {
    println("  #" + i + " -> " + aget(args, i))
  }
}

