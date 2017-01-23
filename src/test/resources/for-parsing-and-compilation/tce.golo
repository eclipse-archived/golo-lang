module TCEOptimized

local function direct = |a, v| {
  if v == 0 {
    return a
  }
  return direct(a + v, v - 1)
}

local function withMatch = |a, v| -> match {
  when v == 0 then a
  otherwise withMatch(a + v, v - 1)
}

local function lambda = |n| {
  let rec = |a, v| {
    if v == 0 {
      return a
    }
    return rec(a + v, v - 1)
  }
  return rec(0, n)
}

local function noopt = |a, v| {
  var r = null
  if v == 0 {
    r = a
  } else {
    r = noopt(a + v, v - 1)
  }
  return r
}

local function withClosure = |a, v, c| -> match {
  when v == 0 then a
  otherwise withClosure(c(a, v), v - 1, c)
}

local function run = |closure, name| {
  let r = box(null)
  let t = Thread(null, {
    require(closure() == 500500, "bad result for " + name)
  }, name, 262144_L)
  t: setUncaughtExceptionHandler(|t, e| {
    r: set(e)
  })
  t: start()
  t: join()
  if r: get() isnt null {
    throw r: get()
  }
}

function test_direct = {
  run(-> direct(0, 1000), "direct")
}

function test_match = {
  run(-> withMatch(0, 1000), "match")
}

function test_lambda = {
  run(-> lambda(1000), "lambda")
}

function test_with_closure = {
  let i = 1
  let f = |a, v| -> i * (a + v)
  run(-> withClosure(0, 1000, f), "closure")
}

function test_noopt = {
  try {
    run(-> noopt(0, 1000), "noopt")
    raise("should fail")
  } catch (e) {
    require(e oftype java.lang.StackOverflowError.class, "not a stack overflow")
  }
}

function main = |args| {
  test_direct()
  test_match()
  test_lambda()
  test_with_closure()
  test_noopt()
}
