module golotest.execution.Overloading

pimp java.lang.String {
  function plus = |this, str| -> this + str
  function plus = |this, str1, str2| -> this + str1 + str2
}

function foo = -> "foo"
function foo = |a| -> a

function pimp1 = -> "a": plus("b")
function pimp2 = -> "a": plus("b", "c")
