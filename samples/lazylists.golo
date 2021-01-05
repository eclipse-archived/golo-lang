# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.LazyLists

import gololang.LazyLists

----
create a lazy list of all integers
----
function count = |start| -> cons(start, -> cons(start + 1))

function useCount = {

  # the list of all even multiples of 3
  let l = count(): map(|x| -> 3 * x): filter(|x| -> (x % 2) == 0)
  # nothing is computed yet

  foreach v in l: take(10) {
    println(v)  # print the 10 first even multiples of 3
  }
}


----
create a lazy range of integers
----
function lazyRange = |start, end| -> match {
  when start >= end then emptyList()
  otherwise cons(start, -> lazyRange(start + 1, end))
}

----
reimplementation of the map function
----
function myMap = |lst, func| -> match {
  when lst: isEmpty() then emptyList()
  otherwise cons(func(lst: head()), -> myMap(lst: tail(), func))
}

# a lazy list of known values
let lst1 = cons(1, cons(2, cons(3, cons(4, emptyList()))))

# or simpler
let lst2 = lazyList(1, 2, 3, 4)

# alternative count using a generator
function countGen = |start| -> generator(|x| -> [x, x + 1], |_| -> false, start)

# alternative count using the iterate generator
function countIter = |start| -> iterate(start, |x| -> x + 1)
