  ----
  Waoo, this is a documented module!

  This is super *cool*.

  Imagine:

      let foo = "bar"
      let bang = "----"
      let bangbang = "------"

      ----
module Documented

import java.util.Map
----
This should be hidden, really.
----
local function should_be_hidden = |foo| -> foo

----
Incredible: this makes a sum.

# Example:

    require(with_doc(18, 24) == 42, "err")

Parameters:

- `a`: first operand,
- `b`: second operand.

Returns: the sum of `a` and `b`.
----
function with_doc = |a, b| -> a + b

----
----
function with_empty_doc = -> 42

----
Improves instances of `String`, such as in:

    println("foo": yop())

----
augment java.lang.String {

  function plop = |this| -> "Plop"

  ----
  The yop factor.
  ----
  function yop = |this| -> "Yop: " + this
}

augment java.lang.String {
  function zig = |this, x| -> "zag"
}

----
A point, with `x` and `y` coordinates.

Do not hesitate to provide an augmentation for `Documented.types.Point` if you
would like to add *methods* to it.
----
struct Point = { x, y }

let letState = 1

var varState = []
