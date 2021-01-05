# ............................................................................................... #
#
# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
#
# ............................................................................................... #
----
This module provides some factory functions and augmentations on IR elements
to improve fluent IR building.

See the [`gololang.ir`](../../javadoc/gololang/ir/package-summary.html) java
package javadoc for documentation on the IR elements themselves.
See the [`gololang.ir.Quote`](./ir/Quote.html) module for an alternative way to create IR nodes.
----
module gololang.ir.DSL

import java.lang.reflect
import org.eclipse.golo.compiler
import gololang.ir


#== # Toplevel elements =======================================================

----
Creates a module with the given name.

For instance:
```golo
`module("MyModule"): `with(
  `struct("Foo"): members("bar", "baz"),
  `function("foo"): withParameters("x")
    : returns(plus(refLookup("x"), constant(1))))
```
creates
```golo
module MyModule

struct Foo = { bar, baz }

function foo = |x| -> x + 1
```

- *param* `name` the name of the module.
----
function `module = |name| -> GoloModule.create(PackageAndClass.of(name), null)

augment gololang.ir.GoloModule {
  ----
  Adds all the given elements to the module.

  - *returns* the module itself
  ----
  function `with = |this, elements...| {
    foreach element in elements {
      this: add(element)
    }
    return this
  }
}


----
Creates an IR [`import`](../../javadoc/gololang/ir/ModuleImport.html) node.

- *param* `mod`: the module to import.

See [`ModuleImport.of`](../../javadoc/gololang/ir/ModuleImport.html#of-java.lang.Object-)
----
function `import = |mod| -> ModuleImport.of(mod)


----
Utility augmentation on all IR elements.
----
augment gololang.ir.GoloElement {
  ----
  Prints a representation of this node subtree to the terminal.
  ----
  function dump = |this| -> this: accept(IrTreeDumper())
}

----
Create a container for top-level elements

See [`ToplevelElements.of`](../../javadoc/gololang/ir/ToplevelElements.html#of-java.lang.Object...-)
----
function toplevels = |elements...| -> ToplevelElements.of(elements)


#== ## Types ------------------------------------------------------------------

----
Creates a structure
----
function `struct = |name| -> Struct.`struct(name)


----
Create an union type
----
function `union = |name| -> Union.`union(name)


----
Creates a member for structures and union values.

Builder API on these objects implicitly create the member when needed.
However, this builder can be useful if one wants to customize the member (e.g. by adding a golodoc).
----
function member = |name| -> Member.of(name)


#== ## Functions --------------------------------------------------------------

----
Create a function decorator from the given expression.

- *param* `expr` the expression representing the decorator or any element that can be converted into a valid
`ExpressionStatement`
- *returns* a [`Decorator`](../../javadoc/gololang/ir/Decorator.html)

See
[`Decorator.of`](../../javadoc/gololang/ir/Decorator.html#of-java.lang.Object-)
----
function decorator = |expr| -> Decorator.of(expr)


----
Creates a function declaration

- *param* `name`: the name of the function
- *returns* a [`GoloFunction`](../../javadoc/gololang/ir/GoloFunction.html)

See
[`GoloFunction.function`](../../javadoc/gololang/ir/GoloFunction.html#function-java.lang.Object-)
and [`lambda`](#lambda_1v)
----
function `function = |name| -> GoloFunction.`function(name)

----
Create a macro declaration.

This is just a function with a macro flag set.

See [`function`](#function_1)
----
function `macro = |name| -> `function(name): asMacro()


#== ## Augmentations ----------------------------------------------------------

----
Creates an augmentation on the target name.

Typical usage:
```golo
`augment(String.class): with(
  `function("foo"): withParameters("this")
    : returns(42))
```
creates
```golo
augment java.lang.String {
   function foo = |this| -> 42
}
```

- *param* `target` the name of the target (compatible with `PackageAndClass.of`)
- *return* a classical augmentation (`Augmentation`)
----
function `augment = |target| -> Augmentation.of(target)


----
Creates a named augmentation.

Typical usage:
```golo
`augmentation("Fooable"): add(
  `function("foo"): withParameters("this")
    : returns(42))
```
creates
```golo
augmentation Fooable = {
   function foo = |this| -> 42
}
```

- *param* `name`: the name of the augmentation (compatible with `PackageAndClass.of`)
- *return* a named augmentation
----
function `augmentation = |name| -> NamedAugmentation.of(name)


#== # Control structures ======================================================

#== ## Exceptions -------------------------------------------------------------

----
Creates a [`try...catch...finally` statement](../../javadoc/gololang/ir/TryCatchFinally.html) IR node.

For instance:
```golo
`try(
  `let("a", 10),
  call("println"): withArgs(refLookup("a"))
): `catch("ex",
  call("println"): withArgs(constant("An error occured"))
  call("println"): withArgs(refLookup("ex"))
): `finally(
  call("cleanUp")
)
```
creates
```golo
try {
  let a = 10
  println(a)
} catch(ex) {
  println("An error occured")
  println(ex)
} finally {
  cleanUp()
}
```

- *param* `statements`: the statements in the `try` block.
- *returns* a `TryCatchFinally` node

See [`TryCatchFinally`](../../javadoc/gololang/ir/TryCatchFinally.html) and the
corresponding [augmentation](#augment.gololang.ir.TryCatchFinally)
----
function `try = |statements...| -> TryCatchFinally.tryCatch()
  : trying(Block.block(statements))

----
See [`TryCatchFinally`](../../javadoc/gololang/ir/TryCatchFinally.html) and the
corresponding [factory](#try_1v)
----
augment gololang.ir.TryCatchFinally {

  ----
  Defines the exception name and the statements in the `catch` block.

  - *param* `exceptionName`: the name of the variable holding the exception.
  - *param* `statements`: the statements in the `catch` block.
  ----
  function `catch = |this, exceptionName, statements...| -> this
      : catching(exceptionName, Block.block(statements))

  ----
  Defines the `finally` block.

  - *param* `statements`: the statements in the `finally` block.
  ----
  function `finally = |this, statements...| -> this
      : finalizing(Block.block(statements))
}


#== ## Conditionals -----------------------------------------------------------

----
Creates a conditional branching IR node.

For instance
```golo
if a != 0 {
  println(a)
} else {
  println("zero")
}
```
can be created by
```golo
`if(notEquals(refLookup("a"), 0)): `then(
  call("println"): withArgs(refLookup("a"))
): `else(
  call("println"): withArgs("zero")
)
```

- *param* `cond`: the condition node.
- *returns* a [`ConditionalBranching`](../../javadoc/gololang/ir/ConditionalBranching.html)

See
[`ConditionalBranching`](../../javadoc/gololang/ir/ConditionalBranching.html)
and the associated [augmentation](#augment.gololang.ir.ConditionalBranching)
----
function `if = |cond| -> ConditionalBranching.branch(): condition(cond)

----
Adds more fluent methods to build a conditional branching IR

See
[`ConditionalBranching`](../../javadoc/gololang/ir/ConditionalBranching.html)
and the associated [factory](#if_1)
----
augment gololang.ir.ConditionalBranching {

  ----
  Defines the true block.

  - *param* `statements`: the statements to execute when the condition is `true`. A `Block` is created if needed.

  See
  [`whenTrue`](../../javadoc/gololang/ir/ConditionalBranching.html#whenTrue-java.lang.Object-)
  ----
  function `then = |this, statements...| -> this: whenTrue(Block.block(statements))

  ----
  Returns the true block.
  ----
  function `then = |this| -> this: trueBlock()

  ----
  Defines the false block or nested conditional

  - *param* `statements`: the statements to execute when the condition is `false`. A `Block` is created if needed.

  See
  [`otherwise`](../../javadoc/gololang/ir/ConditionalBranching.html#otherwise-java.lang.Object-)
  ----
  function `else = |this, statements...| -> match {
    when statements: size() == 1 then this: `otherwise(statements: get(0))
    otherwise this: `otherwise(Block.block(statements))
  }

  ----
  Returns the false block or the nested branch.
  ----
  function `else = |this| -> match {
    when this: hasFalseBlock() then this: falseBlock()
    otherwise this: elseConditionalBranching()
  }
}


----
Creates a [`case`](../../javadoc/gololang/ir/CaseStatement.html) statement.

For instance
```golo
`case()
  : `when(equals(refLookup("x"), 0)): do(
    call("println"): withArgs(constant("null"))
  )
  : `otherwise(
    call("println").withArgs(constant("not null"))
  )
```

creates

```golo
case {
  when x == 0 {
    println("null")
  }
  otherwise {
    println("not null")
  }
}
```

See [`CaseStatement`](../../javadoc/gololang/ir/CaseStatement.html) and
associated [augmentation](#augment.gololang.ir.CaseStatement)
----
function `case = -> CaseStatement.cases()

----
Adds more fluent methods to build a `case` statement.

See [`CaseStatement`](../../javadoc/gololang/ir/CaseStatement.html) and
associated [factory](#case_0)
----
augment gololang.ir.CaseStatement {

  ----
  Define the action for a `when` clause.

  - *param* `statements`: the statements representing the actions in the block. A `Block` is created if needed.

  See [`then`](../../javadoc/gololang/ir/CaseStatement.html#then-java.lang.Object-)
  ----
  function do = |this, statements...| -> this: `then(Block.block(statements))
}


----
Creates a match expression.

Typical usage:
```golo
`let("r", match()
  : when(refLookup("x": equalsTo(0)): then(constant("null"))
  : otherwise(constant("non null")))
```
creates
```golo
let r = match {
  when x == 0 then "null"
  otherwise "non null"
}
```

See [`MatchExpression`](../../javadoc/gololang/ir/MatchExpression.html)
----
function `match = -> MatchExpression.`match()


#== ## Loops ------------------------------------------------------------------

----
Creates a [`for` loop](../../javadoc/gololang/ir/LoopStatement.html) IR node.

For instance
```golo
`for(`var("x", 0),
     lessThan(refLookup("x"), 10),
     assign(plus(refLookup("x"), 1)): to("x")): do(
  call("println"): withArgs(refLookup("x"))
)
```

creates

```golo
for (var x = 0, x < 10, x = x + 1) {
  println(x)
}
```

- *param* `init`: the node representing the loop variable initialization.
- *param* `cond`: the node representing the loop halting condition.
- *param* `post`: the node representing the loop variable increment.
- *returns* a [`LoopStatement`](../../javadoc/gololang/ir/LoopStatement.html)

See [`LoopStatement`](../../javadoc/gololang/ir/LoopStatement.html)
and the [`BlockContainer` augmentation](#augment.gololang.ir.BlockContainer)
----
function `for = |init, cond, post| -> LoopStatement.loop()
  : init(init): condition(cond): post(post)


----
Creates a [`while` loop](../../javadoc/gololang/ir/LoopStatement.html) IR node.

For instance
```golo
block(
  `var("x", 0),
  `while(lessThan(refLookup("x"), 10)): do(
    call("println"): withArgs(refLookup("x")),
    assign(plus(refLookup("x"), 1)): to("x")
  )
)
```

creates

```golo
var x = 0
while x < 10 {
  println(x)
  x = x + 1
}
```

- *param* `cond`: the node representing the `while` condition.
- *returns* a [`LoopStatement`](../../javadoc/gololang/ir/LoopStatement.html)

See [`LoopStatement`](../../javadoc/gololang/ir/LoopStatement.html)
and the [`BlockContainer` augmentation](#augment.gololang.ir.BlockContainer)
----
function `while = |cond| -> LoopStatement.loop(): condition(cond)


----
Creates a [`foreach` loop](../../javadoc/gololang/ir/ForEachLoopStatement.html) IR node.

Typical usage:
```golo
`foreach("x"): in(call("range"): withArgs(5)): do(
  call("println").withArgs(refLookup("x"))
)
```
creates
```golo
foreach x in range(5) {
  println(x)
}
```

- *param* `vars`: the loop variables destructured from the iterable elements
- *returns* a [`ForEachLoopStatement`](../../javadoc/gololang/ir/ForEachLoopStatement.html)

See [`ForEachLoopStatement`](../../javadoc/gololang/ir/ForEachLoopStatement.html)
and the [`BlockContainer` augmentation](#augment.gololang.ir.BlockContainer)
----
function `foreach = |vars...| -> ForEachLoopStatement.create(
    false, null, null, Block.empty(), vars)


#== # Expressions =============================================================

#== ## Literals and references ------------------------------------------------

----
Creates a constant expression.

Constant expressions in the IR are numbers, strings or boolean values (namely
literals).

- *param* `value`: the runtime value of this constant expression.
- *returns* a
  [`ConstantStatement`](../../javadoc/gololang/ir/ConstantStatement.html)
----
function constant = |value| -> ConstantStatement.of(value)


----
Creates a reference lookup.

- *param* `name`: the name of the variable to lookup as a string or a [`LocalReference`](../../javadoc/gololang/ir/LocalReference.html)
- *returns* a
  [`ReferenceLookup`](../../javadoc/gololang/ir/ReferenceLookup.html)

See [`LocalReference`](../../javadoc/gololang/ir/LocalReference.html)
----
function refLookup = |name| -> ReferenceLookup.of(name)


----
Create a local reference with a generated name.

- *returns* a new [`LocalReference`](../../javadoc/gololang/ir/LocalReference.html)
----
function localRef = -> LocalReference.generate()

----
Create a local reference with the name.

- *param* `name`: the name of the reference as a string or a
  [`ReferenceLookup`](../../javadoc/gololang/ir/ReferenceLookup.html)
- *returns* a new [`LocalReference`](../../javadoc/gololang/ir/LocalReference.html)
----
function localRef = |name| -> LocalReference.of(name)

----
Create a local reference with a generated name.

- *returns* a new [`LocalReference`](../../javadoc/gololang/ir/LocalReference.html)
----
function localRef = |name, kind| -> LocalReference.create(name, kind)



#== ## Lambda, function ref, and invocations ----------------------------------

----
Creates an anonymous function.

Typical usage:
```golo
lambda("a", "b"): returns(plus(refLookup("a"), refLookup("b")))
```
creates
```golo
|a, b| -> a + b
```

- *param* `parameters`: the parameters names
- *return* the reference to the anonymous function
----
function lambda = |parameters...| -> GoloFunction.`function(null)
    : withParameters(parameters: asList())
    : synthetic()
    : asClosure()
    : asClosureReference()


----
Creates a reference to the named function.

For instance:
```golo
functionRef("foo")
```
creates
```golo
^foo
```

- *param* `func` the function description. Can be a `String` for the function
  name, a `GoloFunction` or a `java.lang.reflect.Method`.
- *returns* a constant statement containing the function reference.
----
function functionRef = |func| -> match {
  when func oftype GoloFunction.class then functionRef(
      func: enclosingModule(),
      func: name(),
      func: arity(),
      func: isVarargs())
  when func oftype Method.class then functionRef(
    func: declaringClass(),
    func: name(),
    func: parameterCount(),
    func: isVarArgs())
  otherwise functionRef(null, func: toString(), -1, false)
}

----
Creates a reference to the named function and module.

- *param* `mod` a `String` giving the module name, the `GoloModule`
  itself, or the `java.lang.Class` of the module.
- *param* `func` the name of the function to reference.
- *returns* a constant statement containing the function reference.
----
function functionRef = |mod, func| -> functionRef(mod, func, -1, false)

----
Creates a reference to the named function and module with the given arity.

For instance:
```golo
functionRef(java.util.Objects.class, "equals", 2)
```
creates
```golo
^java.util.Objects::equals\2
```

- *param* `mod` a `String` giving the module name, the `GoloModule`
  itself, or the `java.lang.Class` of the module.
- *param* `func` the name of the function to reference.
- *param* `arity` the arity of the referenced function.
- *returns* a constant statement containing the function reference.
----
function functionRef = |mod, func, arity| -> functionRef(mod, func, -1, false)

----
Creates a reference to the named function and module with the given (variable) arity.

For instance:
```golo
functionRef("java.util.Arrays", "asList", 1, true)
```
creates
```golo
^java.util.Arrays::asList\1...
```

- *param* `mod` a `String` giving the module name, the `GoloModule`
  itself, or the `java.lang.Class` of the module.
- *param* `func` the name of the function to reference.
- *param* `arity` the arity of the referenced function.
- *param* `varargs` whether the function is varargs or not.
- *returns* a constant statement containing the function reference.
----
function functionRef = |mod, func, arity, varargs| -> ConstantStatement.of(
    FunctionRef.of(
        match {
          when mod oftype Class.class then mod: canonicalName()
          when mod oftype GoloModule.class then mod: packageAndClass(): toString()
          otherwise mod: toString()
        },
        func,
        arity,
        varargs))


----
Call a function.

Delegates on `FunctionInvocation.of`.

- *returns* a `FunctionInvocation`
----
function call = |fun| -> FunctionInvocation.of(fun)


----
Invoke a method.

Delegates on `MethodInvocation.invoke`.

- *returns* a `MethodInvocation`
----
function invoke = |meth| -> MethodInvocation.invoke(meth)


----
Call a macro.

- *param* `name`: the name of the macro
- *returns* a [`MacroInvocation`](../../javadoc/gololang/ir/MacroInvocation.html)
----
function macroCall = |name| -> MacroInvocation.call(name)


----
Creates a named argument in a call.

For instance:
```golo
call("foo"): withArgs(
  namedArgument("b", 42),
  namedArgument("a", "answer"))
```
creates
```golo
foo(b=42, a="answer")
```

- *param* `name`: the name of the argument
- *param* `value`: the expression node representing the value
- *returns* a [`NamedArgument`](../../javadoc/gololang/ir/NamedArgument.html)
----
function namedArgument = |name, value| -> NamedArgument.of(name, value)


#== ## Operators --------------------------------------------------------------

----
Creates a logical negation operation `not`

- *param* `value`: the expression to negate
- *returns* a [`UnaryOperation`](../../javadoc/gololang/ir/UnaryOperation.html)
----
function `not = |value| -> UnaryOperation.create(OperatorType.NOT(), value)


----
Creates an addition binary operation `+`

- *param* `left`: the left expression
- *param* `right`: the right expression
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function plus = |left, right| -> BinaryOperation.create(OperatorType.PLUS(), left, right)


----
Creates a multiplication binary operation `*`

- *param* `left`: the left expression
- *param* `right`: the right expression
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function times = |left, right| -> BinaryOperation.create(OperatorType.TIMES(), left, right)


----
Creates a subtraction binary operation `-`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function minus = |left, right| -> BinaryOperation.create(OperatorType.MINUS(), left, right)


----
Creates a division binary operation `/`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function divide = |left, right| -> BinaryOperation.create(OperatorType.DIVIDE(), left, right)


----
Creates a modulo binary operation `%`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function modulo = |left, right| -> BinaryOperation.create(OperatorType.MODULO(), left, right)


----
Creates a equality binary operation `==`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function equals = |left, right| -> BinaryOperation.create(OperatorType.EQUALS(), left, right)


----
Creates a difference binary operation `!=`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function notEquals = |left, right| -> BinaryOperation.create(OperatorType.NOTEQUALS(), left, right)


----
Creates a lesser strict comparison binary operation `<`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function lessThan = |left, right| -> BinaryOperation.create(OperatorType.LESS(), left, right)


----
Creates a greater strict comparison binary operation `>`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function moreThan = |left, right| -> BinaryOperation.create(OperatorType.MORE(), left, right)


----
Creates a lesser comparison binary operation `<=`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function lessOrEquals = |left, right| -> BinaryOperation.create(OperatorType.LESSOREQUALS(), left, right)


----
Creates a greater comparison binary operation `>=`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function moreOrEquals = |left, right| -> BinaryOperation.create(OperatorType.MOREOREQUALS(), left, right)


----
Creates a logical conjunction operation `and`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function `and = |left, right| -> BinaryOperation.create(OperatorType.AND(), left, right)


----
Creates a logical disjunction operation `or`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function `or = |left, right| -> BinaryOperation.create(OperatorType.OR(), left, right)


----
Creates a null-checking binary operation `orIfNull`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function `orIfNull = |left, right| -> BinaryOperation.create(OperatorType.ORIFNULL(), left, right)


----
Creates a type-checking binary operation `oftype`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function `oftype = |left, right| -> BinaryOperation.create(OperatorType.OFTYPE(), left, right)


----
Creates an identity equality binary operation `is`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function `is = |left, right| -> BinaryOperation.create(OperatorType.IS(), left, right)


----
Creates an identity difference binary operation `isnt`.

- *param* `left`: the left expression.
- *param* `right`: the right expression.
- *returns* a [`BinaryOperation`](../../javadoc/gololang/ir/BinaryOperation.html)

See also the [`ExpressionStatement` augmentation](#augment.gololang.ir.ExpressionStatement)
----
function `isnt = |left, right| -> BinaryOperation.create(OperatorType.ISNT(), left, right)


----
Allows to use operator factories as methods on expressions, to look more like
infix operator.

For instance, `plus(constant(38), constant(4))` can be written
`constant(38): plus(constant(4))`

See [`ExpressionStatement`](../../javadoc/gololang/ir/ExpressionStatement.html)
----
augment gololang.ir.ExpressionStatement {
  ----
  See [`and`](#and_2)
  ----
  function `and = |this, other| -> gololang.ir.DSL.`and(this, other)

  ----
  See [`divide`](#divide_2)
  ----
  function divide = |this, other| -> gololang.ir.DSL.divide(this, other)

  ----
  See [`equals`](#equals_2)
  ----
  function equalsTo = |this, other| -> gololang.ir.DSL.equals(this, other)

  ----
  See [`is`](#is_2)
  ----
  function `is = |this, other| -> gololang.ir.DSL.`is(this, other)

  ----
  See [`isnt`](#isnt_2)
  ----
  function `isnt = |this, other| -> gololang.ir.DSL.`isnt(this, other)

  ----
  See [`lessOrEquals`](#lessOrEquals_2)
  ----
  function lessOrEquals = |this, other| -> gololang.ir.DSL.lessOrEquals(this, other)

  ----
  See [`lessThan`](#lessThan_2)
  ----
  function lessThan = |this, other| -> gololang.ir.DSL.lessThan(this, other)

  ----
  See [`moreOrEquals`](#moreOrEquals_2)
  ----
  function moreOrEquals = |this, other| -> gololang.ir.DSL.moreOrEquals(this, other)

  ----
  See [`moreThan`](#moreThan_2)
  ----
  function moreThan = |this, other| -> gololang.ir.DSL.moreThan(this, other)

  ----
  See [`notEquals`](#notEquals_2)
  ----
  function notEquals = |this, other| -> gololang.ir.DSL.notEquals(this, other)

  ----
  See [`oftype`](#oftype_2)
  ----
  function `oftype = |this, other| -> gololang.ir.DSL.`oftype(this, other)

  ----
  See [`or`](#or_2)
  ----
  function `or = |this, other| -> gololang.ir.DSL.`or(this, other)

  ----
  See [`orIfNull`](#orIfNull_2)
  ----
  function `orIfNull = |this, other| -> gololang.ir.DSL.`orIfNull(this, other)

  ----
  See [`plus`](#plus_2)
  ----
  function plus = |this, other| -> gololang.ir.DSL.plus(this, other)

  ----
  See [`times`](#times_2)
  ----
  function times = |this, other| -> gololang.ir.DSL.times(this, other)
}


#== ## Collections ------------------------------------------------------------

----
Creates a list literal.

```
list(
  constant(42),
  constant(1337),
  call("f"): withArgs(constant("foo"))
)
```
creates

```
list[42, 1337, f("foo")]
```

- *param* `values`: the expressions representing the values in the list
- *returns* a [`CollectionLiteral`](../../javadoc/gololang/ir/CollectionLiteral.html) node
----
function list = |values...| -> CollectionLiteral.create(CollectionLiteral$Type.list(), values)


----
Creates an array literal.

```
array(
  constant(42),
  constant(1337),
  call("f"): withArgs(constant("foo"))
)
```

creates

```
array[42, 1337, f("foo")]
```
- *param* `values`: the expressions representing the values in the array
- *returns* a [`CollectionLiteral`](../../javadoc/gololang/ir/CollectionLiteral.html) node
----
function array = |values...| -> CollectionLiteral.create(CollectionLiteral$Type.array(), values)


----
Creates a set literal.

```
set(
  constant(42),
  constant(1337),
  call("f"): withArgs(constant("foo"))
)
```

creates

```
set[42, 1337, f("foo")]
```

- *param* `values`: the expressions representing the values in the set
- *returns* a [`CollectionLiteral`](../../javadoc/gololang/ir/CollectionLiteral.html) node
----
function set = |values...| -> CollectionLiteral.create(CollectionLiteral$Type.set(), values)


----
Creates a map literal.

For instance:
```golo
map(
  tuple(constant("a"), constant(1)),
  tuple(constant("b"), constant(2))
)
```
creates
```golo
map[
 ["a", 1],
 ["b", 2]
]
```

- *param* `values`: instances of tuple `CollectionLiteral`
- *returns* a [`CollectionLiteral`](../../javadoc/gololang/ir/CollectionLiteral.html) node

See
[`tuple`](#tuple_1v)
----
function map = |values...| -> CollectionLiteral.create(CollectionLiteral$Type.map(), values)


----
Creates a tuple literal.

```
tuple(constant(42), constant(1337))
```

creates

```
[42, 1337]
```

- *param* `values`: the expressions representing the values in the tuple
- *returns* a [`CollectionLiteral`](../../javadoc/gololang/ir/CollectionLiteral.html) node
----
function tuple = |values...| -> CollectionLiteral.create(CollectionLiteral$Type.tuple(), values)


----
Creates a vector literal.

```
vector(
  constant(42),
  constant(1337),
  call("f"): withArgs(constant("foo"))
)
```

creates

```
vector[42, 1337, f("foo")]
```

- *param* `values`: the expressions representing the values in the vector
- *returns* a [`CollectionLiteral`](../../javadoc/gololang/ir/CollectionLiteral.html) node
----
function vector = |values...| -> CollectionLiteral.create(CollectionLiteral$Type.vector(), values)


----
Creates a range literal.

```
range(constant(1), constant(10))
```

creates

```
[1..10]
```

- *param* `values`: the expressions representing the start and end values of the range
- *returns* a [`CollectionLiteral`](../../javadoc/gololang/ir/CollectionLiteral.html) node
----
function range = |values...| -> CollectionLiteral.create(CollectionLiteral$Type.range(), values)


#== ## Collections Comprehension ----------------------------------------------

----
Creates a list comprehension.

For instance:
```golo
listComprehension(
  plus(refLookup("i"), 1),
  `for(`var("i", 0),
       lessThan(refLookup("i"), 10),
       assign(plus(refLookup("i"), 1))
  )
)
```
creates
```golo
list[i + 1 for(var i = 0, i < 10, i = i + 1)]
```

- *param* `expression`: the comprehension expression.
- *param* `loops`: the loops of the comprehension.
- *returns* a [`CollectionComprehension`](../../javadoc/gololang/ir/CollectionComprehension.html) node
----
function listComprehension = |expression, loops...| -> CollectionComprehension.create(
  CollectionLiteral$Type.list(), expression, loops)


----
Creates an array comprehension.

For instance:
```golo
arrayComprehension(
  plus(refLookup("i"), 1),
  `for(`var("i", 0),
       lessThan(refLookup("i"), 10),
       assign(plus(refLookup("i"), 1))
  )
)
```
creates
```golo
array[i + 1 for(var i = 0, i < 10, i = i + 1)]
```

- *param* `expression`: the comprehension expression.
- *param* `loops`: the loops of the comprehension.
- *returns* a [`CollectionComprehension`](../../javadoc/gololang/ir/CollectionComprehension.html) node
----
function arrayComprehension = |expression, loops...| -> CollectionComprehension.create(
  CollectionLiteral$Type.array(), expression, loops)


----
Creates a set comprehension.

For instance:
```golo
setComprehension(
  plus(refLookup("i"), 1),
  `for(`var("i", 0),
       lessThan(refLookup("i"), 10),
       assign(plus(refLookup("i"), 1))
  )
)
```
creates
```golo
set[i + 1 for(var i = 0, i < 10, i = i + 1)]
```

- *param* `expression`: the comprehension expression.
- *param* `loops`: the loops of the comprehension.
- *returns* a [`CollectionComprehension`](../../javadoc/gololang/ir/CollectionComprehension.html) node
----
function setComprehension = |expression, loops...| -> CollectionComprehension.create(
  CollectionLiteral$Type.set(), expression, loops)


----
Creates a tuple comprehension.

For instance:
```golo
tupleComprehension(
  plus(refLookup("i"), 1),
  `for(`var("i", 0),
       lessThan(refLookup("i"), 10),
       assign(plus(refLookup("i"), 1))
  )
)
```
creates
```golo
tuple[i + 1 for(var i = 0, i < 10, i = i + 1)]
```

- *param* `expression`: the comprehension expression.
- *param* `loops`: the loops of the comprehension.
- *returns* a [`CollectionComprehension`](../../javadoc/gololang/ir/CollectionComprehension.html) node
----
function tupleComprehension = |expression, loops...| -> CollectionComprehension.create(
  CollectionLiteral$Type.tuple(), expression, loops)


----
Creates a vector comprehension.

For instance:
```golo
vectorComprehension(
  plus(refLookup("i"), 1),
  `for(`var("i", 0),
       lessThan(refLookup("i"), 10),
       assign(plus(refLookup("i"), 1))
  )
)
```
creates
```golo
vector[i + 1 for(var i = 0, i < 10, i = i + 1)]
```

- *param* `expression`: the comprehension expression.
- *param* `loops`: the loops of the comprehension.
- *returns* a [`CollectionComprehension`](../../javadoc/gololang/ir/CollectionComprehension.html) node
----
function vectorComprehension = |expression, loops...| -> CollectionComprehension.create(
  CollectionLiteral$Type.vector(), expression, loops)


----
Creates a map comprehension.

For instance:
```golo
mapComprehension(
  tuple(refLookup("i"), plus(refLookup("i"), 1)),
  `for(`var("i", 0),
       lessThan(refLookup("i"), 10),
       assign(plus(refLookup("i"), 1))
  )
)
```
creates
```golo
map[ [i, i + 1] for(var i = 0, i < 10, i = i + 1)]
```

- *param* `expression`: the comprehension expression.
- *param* `loops`: the loops of the comprehension.
- *returns* a [`CollectionComprehension`](../../javadoc/gololang/ir/CollectionComprehension.html) node
----
function mapComprehension = |expression, loops...| -> CollectionComprehension.create(
  CollectionLiteral$Type.map(), expression, loops)



#== # Statements ==============================================================


----
Creates a block containing the given statements.

If only one statement is given and it is a block, it is returned unchanged.

- *param* `statements`: the statements to build the block from.
- *returns* a [`Block`](../../javadoc/gololang/ir/Block.html)
----
function block = |statements...| -> Block.block(statements)

----
Adds more fluent methods to build a block container.
----
augment gololang.ir.BlockContainer {

  ----
  Defines the block as the given statements

  See
  [`body`](../../javadoc/gololang/ir/BlockContainer.html#body-java.lang.Object...-)
  ----
  function do = |this, statements...| -> this: body(statements)
}


#== ## Variable assignment ----------------------------------------------------

local function createAssign = |name| -> match {
  when name oftype Tuple.class then DestructuringAssignment.create()
      : to(name: toArray())
  otherwise AssignmentStatement.create(): to(name)
}


----
Creates a `var` declaration.

For instance:
```golo
`var("answer", 42)
```
creates
```golo
var answer = 42
```

- *param* `name`: the name of the variable to define, or a tuple for destructuring
- *param* `val`: the value to assign to the variable.
- *returns* an [`AssignmentStatement`](../../javadoc/gololang/ir/AssignmentStatement.html)
  or a [`DestructuringAssignment`](../../javadoc/gololang/ir/DestructuringAssignment.html)
----
function `var = |name, val| ->  createAssign(name): as(val): declaring(true): variable()


----
Creates a `let` declaration.

For instance:
```golo
`let("answer", 42)
```
creates
```golo
let answer = 42
```

- *param* `name`: the name of the variable to define, or a tuple for destructuring
- *param* `val`: the value to assign to the variable.
- *returns* an [`AssignmentStatement`](../../javadoc/gololang/ir/AssignmentStatement.html)
  or a [`DestructuringAssignment`](../../javadoc/gololang/ir/DestructuringAssignment.html)
----
function `let = |name, val| -> createAssign(name): as(val): declaring(true)


----
Creates an assignment statement.

For instance:
```golo
assign("foo", 42)
```
creates
```golo
foo = 42
```
- *param* `name`: the name of the variable to define, or a tuple for destructuring
- *param* `val`: the value to assign to the variable.
- *returns* an [`AssignmentStatement`](../../javadoc/gololang/ir/AssignmentStatement.html)
  or a [`DestructuringAssignment`](../../javadoc/gololang/ir/DestructuringAssignment.html)
----
function assign = |name, value| -> createAssign(name): as(value): declaring(false)


----
Create a declaring assignment statement.

For instance:
```golo
let answer = 42
```
can be created with
```golo
define("answer"): as(42)
```
- *param* `name`: the name of the variable to define, or a tuple for destructuring
- *returns* an [`AssignmentStatement`](../../javadoc/gololang/ir/AssignmentStatement.html)
  or a [`DestructuringAssignment`](../../javadoc/gololang/ir/DestructuringAssignment.html)
----
function define = |name| -> createAssign(name): declaring(true)


----
Create a assignment statement.

For instance:
```golo
answer = 42
```
can be created with
```golo
assign(42): to("foo")
```
- *param* `value`: the value to assign to the variable.
- *returns* an [`AssignmentStatement`](../../javadoc/gololang/ir/AssignmentStatement.html)
  or a [`DestructuringAssignment`](../../javadoc/gololang/ir/DestructuringAssignment.html)
----
function assign = |value| -> AssignmentStatement.create(): as(value)


----
Create a destructuring assignment statement.

For instance:
```golo
a, b = foo()
```
can be created with
```golo
destruct(call("foo")): to("a", "b")
```
- *param* `value`: the value to assign to the variable.
- *returns* an [`AssignmentStatement`](../../javadoc/gololang/ir/AssignmentStatement.html)
  or a [`DestructuringAssignment`](../../javadoc/gololang/ir/DestructuringAssignment.html)
----
function destruct = |value| -> DestructuringAssignment.create(): as(value)



#== ## Exit statements --------------------------------------------------------

----
Creates a `throw` statement.
----
function `throw = |ex| -> ThrowStatement.of(ex)


----
Creates a `return` statement.
----
function `return = |value| -> ReturnStatement.of(value)


----
Creates a `break` statement.
----
function `break = -> LoopBreakFlowStatement.newBreak()


----
Creates a `continue` statement.
----
function `continue = -> LoopBreakFlowStatement.newContinue()


#== ## Comments ---------------------------------------------------------------

----
Void statement.

Since this statement is ignored, it can be used to replace a statement in the tree
instead of removing it.
----
function noop = -> Noop.of(null)

----
Void statement containing a message.

Same as [`noop`](#noop_0), but the message can be used by diagnose or debug
tools.
----
function comment = |message| -> Noop.of(message)

