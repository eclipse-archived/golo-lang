module hello.world

function main = |args| {

  try {
    println(10/0)
  } catch (exception) {
    println(exception)
  } finally {
    println ("that's the end")
  }

}

