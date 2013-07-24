module DynamicObjects

function run = {
  let obj = DynamicObject()
  obj: define("acc", 0)
  let random = java.util.Random()
  obj: define("rand", |this| -> random: nextInt())
  for (var i = 0, i < 5_000_000, i = i + 1) {
    obj: acc(obj: acc() + obj: rand())
  }
  return obj: acc()
}