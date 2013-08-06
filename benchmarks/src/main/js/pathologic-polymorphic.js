function run() {
  var data = ["foo", 666, new java.lang.Object(), "bar", 999, new java.util.LinkedList(), new java.util.HashMap(),
              new java.util.TreeSet(), new java.lang.RuntimeException(), new java.lang.IllegalArgumentException(),
              new java.lang.IllegalStateException(), new java.lang.Object(), new java.lang.Exception()];
  var length = data.length;
  var result = null;
  for (var i = 0; i < 200000; i = i + 1) {
    for (var j = 0; j < length; j = j + 1) {
      result = data[j].toString();
     }
  }
  return result;
}