function run() {
  var data = ["foo", 666, "bar", 999, "plop", "da", "plop", "for", "ever",
              1, 2, 3, 4, 5, 6,
              new java.lang.Object(), new java.lang.Object(), new java.lang.Object(), new java.lang.Object()];
  var length = data.length;
  var result = null;
  for (var i = 0; i < 200000; i = i + 1) {
    for (var j = 0; j < length; j = j + 1) {
      result = data[j].toString();
    }
  }
  return result;
}