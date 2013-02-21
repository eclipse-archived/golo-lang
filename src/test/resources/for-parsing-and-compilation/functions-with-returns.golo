module WithReturns

function hello = {
  return
    "Hello"
}

function universalAnswer = { return ((42)) }

function nil = { return null }

function nop = {
  return
}

function many_args = |a, b, c...| { }
