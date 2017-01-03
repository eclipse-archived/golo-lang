module golo.test.SpecialLastArg

local function assertEquals = |value, expected| {
  require(value == expected, "%s should be %s": format(value, expected))
}


# .......................................................................... #

local function noParam = |a, f| -> [a, f()]

function test_noparam = {
  let r = noParam(1) {
    return 2
  }
  assertEquals(r, [1, 2])
}


# .......................................................................... #
local function apply = |a, f| -> f(a)

function test_apply = {
  let r = apply(21) |x| -> 2 * x
  assertEquals(r, 42)
}


# .......................................................................... #
local function revAppender = |v, l| {
  return l: append(v)
}

function test_onlist = {
  let r = revAppender(3) list[
    1,
    2
  ]
  assertEquals(r, list[1, 2, 3])
}


# .......................................................................... #
local function appender = |l, v| {
  return l: append(v)
}

function test_sideeffect = {
  let l = list[4, 2]
  appender(l) 42
  assertEquals(l, list[4, 2, 42])
}



# .......................................................................... #

struct MyStuct = { x }
augment MyStuct {
  function effectfull = |this, a, f| {
    this: x(): append(f(a))
  }

  function pure = |this, a, f| {
    return this: x(): size() + f(a)
  }
}

function test_sideeffect_method = {
  let f = MyStuct(list[])
  f: effectfull(1) |x| -> x + 1
  assertEquals(f: x(), list[2])
}

function test_pure_method = {
  let f = MyStuct(list[1])
  let r = f: pure(40) |x| -> x + 1
  assertEquals(r, 42)
}

# .......................................................................... #

local function inc = |x| -> x + 1

function test_funref_method = {
  let f = MyStuct(list[1])
  let r = f: pure(40) ^inc
  assertEquals(r, 42)
}

function test_funref_function = {
  let r = apply(41) ^inc
  assertEquals(r, 42)
}

# .......................................................................... #
function main = |args| {
  test_noparam()
  test_apply()
  test_onlist()
  test_sideeffect()
  test_sideeffect_method()
  test_pure_method()
  test_funref_method()
  test_funref_function()

  println("ok")
}
