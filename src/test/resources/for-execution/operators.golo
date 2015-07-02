module golotest.execution.Operators

function plus_one = |a| {
  return a + 1
}

function minus_one = |a| {
  return a - 1
}

function half = |a| {
  return a / 2
}

function twice = |a| {
  return a * 2
}

function compute_92 = {
  return ((1 + 2 + 3) * (5) * 6 + (10 / 2) - 1) / 2
}

function eq = |a, b| {
  return a == b
}

function at_least_5 = |a| {
  if a < 5 {
    return 5
  } else {
    return a
  }
}

function strictly_between_1_and_10 = |a| {
  return 1 < a and a < 10
}

function between_1_and_10_or_20_and_30 = |a| {
  return (a >=1 and a <= 10) or ((a >= 20) and (a <= 30))
}

function neq = |a, b| {
  return not (a == b)
}

function same_ref = |a, b| {
  return a is b
}

function different_ref = |a, b| {
  return a isnt b
}

function special_concat = |a, b, c, d| {
  return "[" + a + ":" + b + ":" + c + ":" + d + "]"
}

function oftype_string = |a| {
  return a oftype java.lang.String.class
}

function average = | zero, items... | {
  let count = items: size()
  var sum = zero
  for (var i = 0, i < count, i = i + 1) {
    sum = sum + items: get(i)
  }
  return sum / count
}

function is_even = |value| {
  return (value % 2) == 0
}

function null_guarded = {
  let map = map[]
  return map: get("bogus") orIfNull "n/a"
}

function polymorphic_number_comparison = {
  let left = list[1, 1_L, 1.0, 1.0_F]
  let right = list[2, 2_L, 1.1, 1.1_F]

  var res = true
  foreach a in left {
    foreach b in right {
      res = res and (not (a == b))
      res = res and (not (b == a))
      res = res and (b != a)
      res = res and (a != b)
      
      res = res and (a <= b)
      res = res and (not (a >= b))
      res = res and (b >= a)
      res = res and (not (b <= a))

      res = res and (a < b)
      res = res and (not (a > b))
      res = res and (b > a)
      res = res and (not (b < a))
    }
  }

  foreach a in left {
    foreach b in left {
      res = res and (a == b)
      res = res and (b == a)
      res = res and (not (a != b))
      res = res and (not (b != a))
      
      res = res and (a <= b)
      res = res and (a >= b)
      res = res and (b >= a)
      res = res and (b <= a)

      res = res and (not (a < b))
      res = res and (not (a > b))
      res = res and (not (b > a))
      res = res and (not (b < a))
    }
  }

  return res
}
