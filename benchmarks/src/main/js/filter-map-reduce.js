var data = new Array(2000000);
for (var i = 0; i < 2000000; i++) {
  data[i] = i % 500;
}

function run() {
  return data.map(function(x) {
    return x + 2;
  }).filter(function(x) {
    return x % 2 == 0;
  }).reduce(function(acc, x) {
    return acc + x;
  }, 0);
}