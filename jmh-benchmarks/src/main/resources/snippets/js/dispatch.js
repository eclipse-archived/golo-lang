function dispatch(data) {
  var result = "";
  for (var i = 0; i < data.length; i++) {
    result = data[i].toString();
  }
  return result;
}
