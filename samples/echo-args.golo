module EchoArgs

function main = |args| {
  for (var i = 0, i < alength(args), i = i + 1) {
    println("#" + i + " -> " + aget(args, i))
  }
}

