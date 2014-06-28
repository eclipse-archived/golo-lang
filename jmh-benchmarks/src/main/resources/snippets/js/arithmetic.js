function gcd(x, y, repeat) {
  var res = 0
  for (var i = 0; i < repeat; i++) {
    var a = x;
    var b = y;
    while (a != b) {
      if (a > b) {
        a = a - b;
      } else {
        b = b - a;
      }
    }
    res = a
  }
  return res;
}
