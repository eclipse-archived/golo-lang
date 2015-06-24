# ............................................................................................... #
#
# Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# ............................................................................................... #

module samples.LazyLists

import gololang.lazylist

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
  otherwise cons(func(lst: head()), -> myMap(lst: tail(), func)))
}

# a lazy list of known values
let lst1 = cons(1, cons(2, cons(3, cons(4, emptyList()))))

# or simpler
let lst2 = lazyList(1, 2, 3, 4)


