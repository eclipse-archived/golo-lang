function run() {
  var result = null;
  for (var i = 0; i < 5000000; i = i + 1) {
    result = "" + i;
  }
  return result;
}