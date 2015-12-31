module golotest.augmentations.VarargsAugments

struct Test = { v }

augment Test {
  function foo = |this, a| -> "fix:" + this: v(): toString() + "-" + a: toString()

  function foo = |this, args...| -> "var:" + this: v(): toString() + args: asList(): toString()
}

local function assertEquals = |f, v| {
  require(f == v, v + " expected, got " + f)
}

function test_dispatch = {
  let f = Test(42)
  assertEquals(f: foo(), "var:42[]")
  assertEquals(f: foo("a"), "fix:42-a")
  assertEquals(f: foo("a", "b"), "var:42[a, b]")
}

function main = |args| {
  test_dispatch()
  println("ok")
}
