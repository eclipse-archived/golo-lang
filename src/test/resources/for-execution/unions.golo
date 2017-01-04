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
    when this is None() then this
    otherwise func(this: value())
  }

  function fmap = |this, func| -> match {
    when this is None() then this
    otherwise Option.Some(func(this: value()))
  }
}

function monadicAdd = |mx, my| ->
  mx: bind(|x| ->
    my: bind(|y| ->
      Some(x + y)))

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
    when this oftype Tree$Node.class then "I'm a node"
    when this oftype Tree$Leaf.class then "I'm a leaf"
    otherwise "I'm empty"
  }
}


# ............................................................................................... #
function test_toString = {
  let n = None()
  let s = Some(5)

  require(n: toString() == "union Option.None", "empty value toString")
  require(s: toString() == "union Option.Some{value=5}", "single valued toString")

  let t = Node(Node(Leaf(1), Empty()), Leaf(0))
  require(t: toString() ==
    "union Tree.Node{left=union Tree.Node{left=union Tree.Leaf{value=1}, right=union Tree.Empty}, right=union Tree.Leaf{value=0}}",
    "recursive tree structure toString")
}

# ............................................................................................... #
function test_hashcode = {
  require(None(): hashCode() == None(): hashCode(),
    "singleton hashcode")
  require(Some("bar"): hashCode() == Some("bar"): hashCode(),
    "hashcode")
  require(Some(5): hashCode() == Some(5): hashCode(), "hashcode")
  require(Some(5): hashCode() != Some("5"): hashCode(), "hashcode")

  let t1 = Node(Node(Leaf(1), Empty()), Leaf(0))
  let t2 = Node(Node(Leaf(1), Empty()), Leaf(0))
  require(t1: hashCode() == t2: hashCode(), "hashcode")

  let t3 = Node(Node(Leaf(3), Empty()), Leaf(0))
  let t4 = Node(Node(Leaf(4), Empty()), Leaf(0))
  require(t3: hashCode() != t4: hashCode(), "hashcode")
}

# ............................................................................................... #
function test_equality = {
  let n = None()
  let s = Some(5)

  require(n == None(), "singleton equality")
  require(n is None(), "singleton identity")

  require(s == Some(5), "value equality")
  require(s isnt Some(5), "value identity")

  let t1 = Node(
    Node(
      Empty(),
      Leaf("foo")
    ),
    Leaf(1)
  )

  let t2 = Node(
    Node(
      Empty(),
      Leaf("foo")
    ),
    Leaf(1)
  )

  require(t1 == t2, "recurive structure equality")

  require(RED() == RED(), "classical union equality")
  require(RED() is RED(), "classical union identity")
  require(RED() != BLUE(), "classical union difference")
  require(RED() isnt BLUE(), "classical union distinct")

}

# ............................................................................................... #
function test_augmentations = {
  let n = None()
  let s = Some(5)

  require(monadicAdd(n, n) == n, "err")
  require(monadicAdd(s, n) == n, "err")
  require(monadicAdd(n, s) == n, "err")
  require(monadicAdd(s, s) == Some(10), "err")

  let double = |x| -> 2 * x
  require(n: fmap(double) == n, "err")
  require(s: fmap(double) == Some(10), "err")

  require(not Leaf(0): isEmptyNode(), "err on Tree.Leaf:isEmpty")
  require(not Node(0, 0): isEmptyNode(), "err on Tree.Node:isEmpty")
  require(Empty(): isEmptyNode(), "err on Tree.Empty:isEmpty")

  require(Leaf(0): whoAreYou() == "I'm a leaf",
    "err on Tree.Leaf:whoAreYou")
  require(Node(0, 0): whoAreYou() == "I'm a node",
    "err on Tree.Node:whoAreYou")
  require(Empty(): whoAreYou() == "I'm empty",
    "err on Tree.Empty:whoAreYou")

}

function test_immutable = {
  let s = Some(2)
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
  let n = Node(0, 0)
  let l = Leaf(0)
  let e = Empty()

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

function test_match_named = {
  let ft = Node(Empty(), Leaf(42))
  require(ft: isNode(Empty(), Leaf(42)), "err")
  # Not Yet: see #351 #352
  # require(ft: isNode(left=Empty(), right=Leaf(42)), "err")
  # require(ft: isNode(right=Leaf(42), left=Empty()), "err")
}
# ............................................................................................... #

union U = {
  Abc = { abc }
}

function test_same_attribute_name = {
  let abc = Abc("abc")
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
  test_match_named()
  test_same_attribute_name()

  println("OK")
}
