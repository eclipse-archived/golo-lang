
module Test

local function assertEquals = |got, expected| {
  require(expected == got, "expected %s, got %s": format(expected, got))
}

function yo = |message| -> |before, after| -> before + ":" + message + ":" + after

function say = |message, before, after| -> before + ":" + message + ":" + after

function add = |left, right| -> left + right

function test_indirect = {
  let expected =  "Hello:sam:!"

  let y = yo("sam")
  assertEquals(y(before="Hello", after="!"), expected)

  let s = ^say: bindAt("message", "sam")
  assertEquals(s(before="Hello", after="!"), expected)

  let a = ^add
  assertEquals(a(left="a", right="b"), "ab")

  let l = |left, right| -> left + right
  assertEquals(l(left="a", right="b"), "ab")

}


function test_no_names = {
  let expected =  "Hello:sam:!"

  assertEquals(yo("sam")("Hello", "!"), expected)
  assertEquals(^say: bindAt("message", "sam")("Hello", "!"), expected)
  assertEquals(add("a", "b"), "ab")
  assertEquals(^add("a", "b"), "ab")
  assertEquals((|left, right| -> left + right)("a", "b"), "ab")
}

function test_anon_names = {
  let expected =  "Hello:sam:!"
  assertEquals(yo("sam")(before="Hello", after="!"), expected)
  assertEquals(^say: bindAt("message", "sam")(before="Hello", after="!"), expected)
}

function test_ref_names = {
  assertEquals(^add(left="a", right="b"), "ab")
  assertEquals((|left, right| -> left + right)(left="a", right="b"), "ab")
}


function main = |args| {
  assertEquals(add(left="a", right="b"), "ab")
  test_indirect()
  test_no_names()
  test_anon_names()
  test_ref_names()

  println("ok")
}
