function run() {
  var obj = new Object();
  obj.acc = 0;
  var random = new java.util.Random();
  obj.rand = function() { return random.nextInt(); }
  for (var i = 0; i < 5000000; i = i + 1) {
    obj.acc = obj.acc + obj.rand();
  }
  return obj.acc;
}