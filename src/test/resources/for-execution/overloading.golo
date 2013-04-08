module golotest.execution.Overloading

augment java.lang.String {
  function plus = |this, str| -> this + str
  function plus = |this, str1, str2| -> this + str1 + str2
}

function foo = -> "foo"
function foo = |a| -> a

function augmentation1 = -> "a": plus("b")
function augmentation2 = -> "a": plus("b", "c")
