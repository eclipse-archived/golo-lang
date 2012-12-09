module samples.MaxInt

local function max_int = {
  return java.lang.Integer.MAX_VALUE()
}

function main = |args| {
  println(max_int())
}

