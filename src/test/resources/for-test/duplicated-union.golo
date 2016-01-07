
module DuplicatedUnion

union Foo = {
  Bar = { x }
  Baz = { x, y }
}

union Foo = {
  Plop = { x }
  Daplop
}

function main = |args| {

}
