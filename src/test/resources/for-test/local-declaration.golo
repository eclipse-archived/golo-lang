
module TestLocalDeclaration

function f = |x| -> match {
  when x <= 0 then 0
  otherwise d with {
    d = 2 * x
  }
}

function g = -> b + (2 * a + 2) with {

  # some comment
  a = 20

  # some comment
  b = "foo"

}


function sum = |lst| -> sum(0, lst) with {
  # private lambda with the accumulator
  sum = |acc, lst| -> match {
    when lst: isEmpty() then acc

    # recursive terminal call
    otherwise sum(acc + head, tail) with {

      # list destructuring
      head, tail... = lst
    }
  }
}

function h = |x| -> match {
  when a < 0 then 1337
  when a > 10 then 2 * a
  otherwise a
} with {
  a = x + 1
}

function comp = |it| -> list[
  a + 1 with { a = x / 2 }
  foreach x in it
]

function comp2 = |i| -> l: size() with {
  l = list[a + 1 foreach a in range(i)]
}

function clos = {
  return (|x| -> |y| -> x + y + c)(a)(b) with {
    a = 1
    b = 2
    c = 3
  }
}

function compo = |x| {
  let c = f: andThen(g) with {
    f = |x| -> "f" + x
    g = |y| -> "g" + y
  }
  return c(x)
}


function test_with = {
  require(f(-1) == 0, "err")
  require(f(21) == 42, "err")
  require(g() == "foo42", "err")

  require(sum(list[1, 2, 3, 4, 5]) == 15, "err")

  require(h(-1) == 0, "err")
  require(h(-2) == 1337, "err")
  require(h(5) == 6, "err")
  require(h(20) == 42, "err")

  require(comp(list[2, 4, 6]) == list[2, 3, 4], "err")

  require(comp2(5) == 5, "err")

  require(clos() == 6, "err")

  require(compo("a") == "gfa", "err")
}

function main = |args| {
  test_with()
}
