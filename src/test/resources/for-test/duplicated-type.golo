
module DuplicatedType

union Foo = {
  Bar = { x }
  Baz = { x, y }
}

struct Foo = { a }

function main = |args| {

}
