module golotest.execution.WrongScope

function a = |n| {
  let plop = "ok"
  if true {
    var flick = plop
  }
  flick = "flack"
}
