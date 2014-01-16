function convert(data) {
  return Java.from(data);
}

function run(data) {
  return data.filter(function(x) {
    return x % 2 == 0;
  }).map(function(x) {
    return x * 2;
  }).reduce(function(acc, x) {
    return acc + x;
  }, 0);
}
