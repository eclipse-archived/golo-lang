
module golo.test.VarargsTest

import org.testng.Assert

&use("golo.test.VarargsMacros")

function plop = |x| -> "plop:" + x

function test = {
  assertEquals(&foo("a", "b", "c", "d"), list["a", "b", "c", "d"])
  assertEquals(&foo("a", "b"), list["a", "b"])

#tag::test_sayHellos[]
  let l = list[]
  sayHellos(l, "foo", answer(), plop(42))
#end::test_sayHellos[]

  assertEquals(l, list["Hello foo", "Hello 42", "Hello plop:42"])
}

function main = |args| {
  test()
  println("ok")
}
