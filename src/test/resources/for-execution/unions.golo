module golotest.execution.Unions

# ............................................................................................... #
union Color = {RED GREEN BLUE}

# ............................................................................................... #
union Option = {
  Some = {value}
  None
}

augment Option {
  function bind = |this, func| -> match {
    when this is Option.None() then this
    otherwise func(this: value())
  }

  function fmap = |this, func| -> match {
    when this is Option.None() then this
    otherwise Option.Some(func(this: value()))
  }
}

function monadicAdd = |mx, my| ->
  mx: bind(|x| ->
    my: bind(|y| ->
      Option.Some(x + y)))

# ............................................................................................... #
union Tree = {
  Node = {left, right}
  Leaf = {value}
  Empty
}

augment Tree$Node {
  function isEmptyNode = |this| -> false
}

augment Tree$Empty {
  function isEmptyNode = |this| -> true
}

augment Tree$Leaf {
  function isEmptyNode = |this| -> false
}

augment Tree {
  function whoAreYou = |this| -> match {
    when this oftype golotest.execution.Unions.types.Tree$Node.class then "I'm a node"
    when this oftype golotest.execution.Unions.types.Tree$Leaf.class then "I'm a leaf"
    otherwise "I'm empty"
  }
}


# ............................................................................................... #
function test_toString = {
  let n = Option.None()
  let s = Option.Some(5)

  require(n: toString() == "union Option.None", "empty value toString")
  require(s: toString() == "union Option.Some{value=5}", "single valued toString")

  let t = Tree.Node(Tree.Node(Tree.Leaf(1), Tree.Empty()), Tree.Leaf(0))
  require(t: toString() ==
    "union Tree.Node{left=union Tree.Node{left=union Tree.Leaf{value=1}, right=union Tree.Empty}, right=union Tree.Leaf{value=0}}",
    "recursive tree structure toString")
}

# ............................................................................................... #
function test_hashcode = {
  require(Option.None(): hashCode() == Option.None(): hashCode(),
    "singleton hashcode")
  require(Option.Some("bar"): hashCode() == Option.Some("bar"): hashCode(),
    "hashcode")
  require(Option.Some(5): hashCode() == Option.Some(5): hashCode(), "hashcode")
  require(Option.Some(5): hashCode() != Option.Some("5"): hashCode(), "hashcode")

  let t1 = Tree.Node(Tree.Node(Tree.Leaf(1), Tree.Empty()), Tree.Leaf(0))
  let t2 = Tree.Node(Tree.Node(Tree.Leaf(1), Tree.Empty()), Tree.Leaf(0))
  require(t1: hashCode() == t2: hashCode(), "hashcode")

  let t3 = Tree.Node(Tree.Node(Tree.Leaf(3), Tree.Empty()), Tree.Leaf(0))
  let t4 = Tree.Node(Tree.Node(Tree.Leaf(4), Tree.Empty()), Tree.Leaf(0))
  require(t3: hashCode() != t4: hashCode(), "hashcode")
}

# ............................................................................................... #
function test_equality = {
  let n = Option.None()
  let s = Option.Some(5)

  require(n == Option.None(), "singleton equality")
  require(n is Option.None(), "singleton identity")

  require(s == Option.Some(5), "value equality")
  require(s isnt Option.Some(5), "value identity")

  let t1 = Tree.Node(
    Tree.Node(
      Tree.Empty(),
      Tree.Leaf("foo")
    ),
    Tree.Leaf(1)
  )

  let t2 = Tree.Node(
    Tree.Node(
      Tree.Empty(),
      Tree.Leaf("foo")
    ),
    Tree.Leaf(1)
  )

  require(t1 == t2, "recurive structure equality")

  require(Color.RED() == Color.RED(), "classical union equality")
  require(Color.RED() is Color.RED(), "classical union identity")
  require(Color.RED() != Color.BLUE(), "classical union difference")
  require(Color.RED() isnt Color.BLUE(), "classical union distinct")

}

# ............................................................................................... #
function test_augmentations = {
  let n = Option.None()
  let s = Option.Some(5)

  require(monadicAdd(n, n) == n, "err")
  require(monadicAdd(s, n) == n, "err")
  require(monadicAdd(n, s) == n, "err")
  require(monadicAdd(s, s) == Option.Some(10), "err")

  let double = |x| -> 2 * x
  require(n: fmap(double) == n, "err")
  require(s: fmap(double) == Option.Some(10), "err")

  require(not Tree.Leaf(0): isEmptyNode(), "err on Tree.Leaf:isEmpty")
  require(not Tree.Node(0, 0): isEmptyNode(), "err on Tree.Node:isEmpty")
  require(Tree.Empty(): isEmptyNode(), "err on Tree.Empty:isEmpty")

  require(Tree.Leaf(0): whoAreYou() == "I'm a leaf",
    "err on Tree.Leaf:whoAreYou")
  require(Tree.Node(0, 0): whoAreYou() == "I'm a node",
    "err on Tree.Node:whoAreYou")
  require(Tree.Empty(): whoAreYou() == "I'm empty",
    "err on Tree.Empty:whoAreYou")

}

function test_immutable = {
  let s = Option.Some(2)
  require(s: value() == 2, "err")
  try {
    s: value(3)
    raise("union values should be immutable")
  } catch (e) {
    require(e oftype java.lang.NoSuchMethodError.class, "bad exception")
  }
}

function test_not_instantiable = {
  try {
    let s = golotest.execution.Unions.types.Tree$Leaf(1)
    raise("union values should not be instanciable")
  } catch (e) {
    require(e oftype java.lang.NoSuchMethodError.class, "bad exception")
  }
}

function test_singleton = {
  try {
    let s = golotest.execution.Unions.types.Tree$Empty()
    raise("union values should not be instanciable")
  } catch (e) {
    require(e oftype java.lang.NoSuchMethodError.class, "bad exception")
  }
}

function test_match_methods = {
  let n = Tree.Node(0, 0)
  let l = Tree.Leaf(0)
  let e = Tree.Empty()

  require(not n: isEmpty(), "err")
  require(not l: isEmpty(), "err")
  require(e: isEmpty(), "err")

  require(n: isNode(), "err")
  require(not l: isNode(), "err")
  require(not e: isNode(), "err")

  require(not n: isLeaf(), "err")
  require(l: isLeaf(), "err")
  require(not e: isLeaf(), "err")

  require(l: isLeaf(0), "err")
  require(not l: isLeaf(42), "err")

  require(n: isNode(0, 0), "err")
  require(not n: isNode(42, 0), "err")
  require(not n: isNode(0, 42), "err")
  require(not n: isNode(42, 42), "err")

  let _ = gololang.Unknown.get()
  require(n: isNode(0, _), "err")
  require(n: isNode(_,0), "err")
  require(n: isNode(_,_), "err")
}
# ............................................................................................... #

union U = {
  Abc = { abc }
}

function test_same_attribute_name = {
  let abc = U.Abc("abc")
  require(abc: abc() == "abc", "err")
  require(abc: isAbc(), "err")
}

# ............................................................................................... #
function main = |args| {
  test_toString()
  test_equality()
  test_hashcode()
  test_augmentations()
  test_immutable()
  test_singleton()
  test_not_instantiable()
  test_match_methods()
  test_same_attribute_name()

  println("OK")
}
