function run() {
  var runner = function(value) {
    return "[" + value + "]";
  };
  var result = null;
  for (var i = 0; i < 2000000; i = i + 1) {
    result = runner(i);
  }
  return ">>> " + result;
}