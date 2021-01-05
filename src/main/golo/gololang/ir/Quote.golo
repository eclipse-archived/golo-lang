# ............................................................................................... #
#
# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
#
# ............................................................................................... #
----
This module provides the [`quote`](#quote_1v) and [`unquote`](#unquote_1) macros
to ease the creation of IR node.

Using this macros is an alternative to creating IR nodes from scratch with the
fluent API. It can be seen as an equivalent of string interpolation for IR
nodes.

For instance, to create a node representing the following code:
```golo
|a, b| -> a + b
```

one can use the [fluent builder API](../ir.html), as in:
```golo
let node = lambda("a", "b"): returns(
            plus(refLookup("a"), refLookup("b")))
```

or the [`quote`](#quote_1v) macro, as in:
```golo
let node = &quote(|a, b| -> a + b)
```

Variable declared in the quoted code are mangled to prevent name clashes. For
instance:
```golo
let node = &quote {
  let tmp = 42
}
```

creates a code equivalent to:
```golo
let node = define("__$$_quoted_tmp"): as(constant(42))
```

Since the mangling function use a
[`SymbolGenerator`](../../../javadoc/org/eclipse/golo/compiler/SymbolGenerator.html),
variables and references whose name starts with `$` are not mangled (but the `$` is removed).

Unquoting can be compared to string interpolation. Unquoted variables containing
IR nodes are injected as is, without being quoted. For instance, to create a
`do while` macro, one can use:
```golo
macro doWhile = |condition, block| -> &quote {
  $(block)
  while $(condition) {
    $(block)
  }
}
```

It is obviously possible to mix the API approach with the quoting one, as in:
```golo
`augment(String.class): `with(&quote {
  function foo = |this| -> 42
})
```
----
module gololang.ir.Quote

import gololang.ir

let SYMBOLS = DynamicVariable(org.eclipse.golo.compiler.SymbolGenerator("quoted"))

local function mangle = |name, protected| -> match {
  when protected is null then name
  when protected: contains(name) then name
  otherwise SYMBOLS: value(): getFor(name)
}

local function augmentProtected = |p, ns| {
  if p is null {
    return null
  }
  let newP = set[]
  newP: addAll(p)
  if ns isnt null {
    newP: addAll(ns)
  }
  return newP
}

local function callIr = |name| ->
  FunctionInvocation.of("gololang.ir." + name)

local function enumValue = |val| -> FunctionInvocation.of(
    val: class(): name() + "." + val: name())

local function quoteNodeArray = |a, p| -> array[quoteNode(e, p) foreach e in a]

local function quoteConstant = |node, p| -> callIr("ConstantStatement.of"): withArgs(node)

local function quoteBlock = |node, p| -> match {
  when node is null or node: isEmpty() then callIr("Block.empty")
  when node: statements(): size() == 1 then quoteNode(node: statements(): get(0), p)
  otherwise quoteLocalDeclaration(
    callIr("Block.block"): withArgs(quoteNodeArray(node: statements(), p)),
    node: declarations(), p)
}

local function quoteBinaryOp = |node, p| -> quoteLocalDeclaration(
  callIr("BinaryOperation.create")
    : withArgs(
      enumValue(node: type()),
      quoteNode(node: left(), p),
      quoteNode(node: right(), p)),
  node: declarations(), p)

local function quoteLocalDeclaration = |builder, declarations, p| {
  var ir = builder
  foreach dec in declarations {
    ir = MethodInvocation.invoke("with")
      : withArgs(quoteAssignment(dec, p))
      : on(ir)
  }
  return ir
}

local function quoteUnaryOp = |node, p| -> quoteLocalDeclaration(
  callIr("UnaryOperation.create")
    : withArgs(
      enumValue(node: type()),
      quoteNode(node: expression(), p)),
  node: declarations(), p)

local function quoteFunctionInvocation = |node, p| -> quoteLocalDeclaration(
  callIr("FunctionInvocation.create")
    : withArgs(
      ConstantStatement.of(match {
        when node: isAnonymous() then null
        otherwise node: name()
      }),
      ConstantStatement.of(node: isOnReference()),
      ConstantStatement.of(node: isOnModuleState()),
      ConstantStatement.of(node: isConstant()))
    : withArgs(quoteNodeArray(node: arguments(), p)),
  node: declarations(), p)

local function quoteReturn = |node, p| -> match {
  when node: isReturningVoid() then callIr("ReturnStatement.empty()")
  otherwise callIr("ReturnStatement.of")
    : withArgs(quoteNode(node: expression(), p))
}

local function quoteThrow = |node, p| -> callIr("ThrowStatement.of")
  : withArgs(quoteNode(node: expression(), p))

local function quoteLocalRef = |node, p| -> callIr("LocalReference.create"): withArgs(
    ConstantStatement.of(mangle(node: name(), p)),
    enumValue(node: kind()))

local function quoteAssignment = |node, p| -> callIr("AssignmentStatement.create")
  : withArgs(
    quoteNode(node: localReference(), p),
    quoteNode(node: expression(), p),
    ConstantStatement.of(node: isDeclaring()))

local function quoteReference = |node, p| -> quoteLocalDeclaration(
  callIr("ReferenceLookup.of"): withArgs(
    ConstantStatement.of(mangle(node: name(), p))),
  node: declarations(), p)

local function quoteCollection = |node, p| -> quoteLocalDeclaration(
  callIr("CollectionLiteral.create")
    : withArgs(enumValue(node: type()))
    : withArgs(quoteNodeArray(node: expressions(), p)),
  node: declarations(), p)

local function quoteModuleImport = |node, p| -> callIr("ModuleImport.of")
  : withArgs(ConstantStatement.of(node: packageAndClass(): toString()))

local function quoteNamedArgument = |node, p| ->
  callIr("NamedArgument.of")
    : withArgs(
        ConstantStatement.of(node: name()),
        quoteNode(node: expression(), p))

local function quoteMember = |node, p| -> callIr("Member.of"): withArgs(
  ConstantStatement.of(node: name()))

local function quoteMethodInvocation = |node, p| -> quoteLocalDeclaration(
  MethodInvocation.invoke("withArgs")
    : withArgs(quoteNodeArray(node: arguments(), p))
    : on(MethodInvocation.invoke("nullSafe")
      : withArgs(ConstantStatement.of(node: isNullSafeGuarded()))
      : on(callIr("DSL.invoke"): withArgs(ConstantStatement.of(node: name())))),
  node: declarations(), p)

local function quoteTryCatch = |node, p| -> callIr("TryCatchFinally.create"): withArgs(
  ConstantStatement.of(match {
    when node: hasCatchBlock() then mangle(node: exceptionId(), p)
    otherwise null
  }),
  quoteBlock(node: tryBlock(), p),
  match {
    when node: hasCatchBlock() then quoteBlock(node: catchBlock(), p)
    otherwise ConstantStatement.of(null)
  },
  match {
    when node: hasFinallyBlock() then quoteBlock(node: finallyBlock(), p)
    otherwise ConstantStatement.of(null)
  })

local function quoteStruct = |node, p| -> MethodInvocation.invoke("members")
  :withArgs(quoteNodeArray(node: getMembers(), p))
  : on(callIr("Struct.struct"): withArgs(ConstantStatement.of(node: name())))

local function quoteUnion = |node, p| {
  var ir = callIr("Union.union"): withArgs(ConstantStatement.of(node: name()))
  foreach val in node: values() {
    ir = MethodInvocation.invoke("value")
      : withArgs(ConstantStatement.of(val: name()))
      : withArgs(quoteNodeArray(val: getMembers(), p))
      : on(ir)
  }
  return ir
}

local function quoteLoopBreak = |node, p| -> match {
  when node: type() is LoopBreakFlowStatement$Type.BREAK() then
    callIr("LoopBreakFlowStatement.newBreak")
  when node: type() is LoopBreakFlowStatement$Type.CONTINUE() then
    callIr("LoopBreakFlowStatement.newContinue")
  otherwise raise("Unknown type")
}

local function quoteLoopStatement = |node, p| -> callIr("LoopStatement.create"): withArgs(
  quoteNode(node: init(), p),
  quoteNode(node: condition(), p),
  quoteNode(node: post(), p),
  quoteNode(node: block(), p))

local function quoteForEachLoop = |node, p| -> callIr("ForEachLoopStatement.create")
  : withArgs(
    ConstantStatement.of(node: isVarargs()),
    quoteNode(node: iterable(), p),
    quoteNode(node: whenClause(), p),
    quoteNode(node: block(), p))
  : withArgs(quoteNodeArray(node: references(), p))

local function quoteWhenClauses = |element, node, p| {
  var ir = MethodInvocation.invoke("otherwise")
    : withArgs(quoteNode(node: `otherwise(), p))
    : on(element)
  foreach c, v in node: clauses() {
    ir = MethodInvocation.invoke("then"): withArgs(quoteNode(v, p))
          : on(MethodInvocation.invoke("when"): withArgs(quoteNode(c, p))
            : on(ir))
  }
  return ir
}

local function quoteConditional = |node, p| -> callIr("ConditionalBranching.create"): withArgs(
  quoteNode(node: condition(), p),
  quoteNode(node: trueBlock(), p),
  quoteNode(node: falseBlock(), p),
  quoteNode(node: elseConditionalBranching(), p))

local function quoteClosure = |node, p| {
  var ir = callIr("DSL.lambda")
    : withArgs(node: target(): parameterNames(): toArray())
  let protected = augmentProtected(p, node: target(): parameterNames())
  if node: isVarargs() {
    ir = MethodInvocation.invoke("varargs"): on(ir)
  }
  ir = MethodInvocation.invoke("block")
      : withArgs(callIr("Block.of")
          : withArgs(quoteNode(node: target(): block(), protected)))
      : on(ir)
  return quoteLocalDeclaration(ir, node: declarations(), p)
}

local function quoteFunction = |node, p| {
  let protected = augmentProtected(p, node: parameterNames())
  var ir = callIr("GoloFunction.function"): withArgs(ConstantStatement.of(node: name()))
  ir = MethodInvocation.invoke("withParameters")
    : withArgs(array[ConstantStatement.of(n) foreach n in node: parameterNames()])
    : on(ir)
  ir = MethodInvocation.invoke("block")
    : withArgs(callIr("Block.of")
      : withArgs(quoteBlock(node: block(), protected)))
    : on(ir)
  if node: isVarargs() {
    ir = MethodInvocation.invoke("varargs"): on(ir)
  }
  if node: isMacro() {
    ir = MethodInvocation.invoke("asMacro"): on(ir)
  }
  if node: isLocal() {
    ir = MethodInvocation.invoke("local"): on(ir)
  }
  if node: isDecorated() {
    ir = MethodInvocation.invoke("decoratedWith")
      : withArgs(quoteNodeArray(node: decorators(), p))
      : on(ir)
  }
  ir = MethodInvocation.invoke("contextual")
    : withArgs(ConstantStatement.of(node: isContextualMacro()))
    : on(ir)
  ir = MethodInvocation.invoke("special")
    : withArgs(ConstantStatement.of(node: isSpecialMacro()))
    : on(ir)
  return ir
}

local function quoteDecorator = |node, p| {
  let protected = augmentProtected(p, null)
  if node: expression() oftype ReferenceLookup.class {
    protected?: add(node: expression(): name())
  }
  return MethodInvocation.invoke("constant")
  : withArgs(ConstantStatement.of(node: isConstant()))
  : on(callIr("Decorator.of")
      : withArgs(quoteNode(node: expression(), protected)))
}

local function quoteAugmentation = |node, p| {
  var ir = callIr("Augmentation.of"): withArgs(ConstantStatement.of(node: target(): toString()))
  ir = MethodInvocation.invoke("with")
    : withArgs(array[ConstantStatement.of(name) foreach name in node: names()])
    : on(ir)
  ir = MethodInvocation.invoke("with")
    : withArgs(quoteNodeArray(node: functions(), p))
    : on(ir)
  return ir
}

local function quoteNamedAugmentation = |node, p| -> MethodInvocation.invoke("add")
    : withArgs(quoteNodeArray(node: functions(), p))
    : on(callIr("NamedAugmentation.of"): withArgs(ConstantStatement.of(node: name())))

local function quoteDestruct = |node, p| {
  var ir = callIr("DestructuringAssignment.destruct"): withArgs(quoteNode(node: expression(), p))
  ir = MethodInvocation.invoke("to")
    : withArgs(quoteNodeArray(node: references(), p))
    : on(ir)
  ir = MethodInvocation.invoke("declaring")
    : withArgs(ConstantStatement.of(node: isDeclaring()))
    : on(ir)
  ir = MethodInvocation.invoke("varargs")
    : withArgs(ConstantStatement.of(node: isVarargs()))
    : on(ir)
  if not node: isConstant() {
    ir = MethodInvocation.invoke("variable"): on(ir)
  }
  return ir
}

local function quoteCollComprehension = |node, p| -> quoteLocalDeclaration(
  callIr("CollectionComprehension.create")
    : withArgs(enumValue(node: type()))
    : withArgs(quoteNode(node: expression(), p))
    : withArgs(quoteNodeArray(node: loops(), p)),
  node: declarations(), p)

----
Quotes the given node by converting it into calls to [IR API](../ir.html).

This function should mainly be used by the [`quote`](#quote_1v) macro and not
called directly.

- *param* `node`: the node to quote
- *param* `p`: a set of names that must not be mangled, or `null` if no name
   must be mangled
- *returns* an expression that evaluate to the quoted node.
----
function quoteNode = |node, p| -> match {
  when node is null then ConstantStatement.of(null)
  when isUnquoted(node) then node: metadata("unquoted", null)
  when node oftype MacroInvocation.class then node
  when node oftype ConstantStatement.class then quoteConstant(node, p)
  when node oftype Block.class then quoteBlock(node, p)
  when node oftype BinaryOperation.class then quoteBinaryOp(node, p)
  when node oftype UnaryOperation.class then quoteUnaryOp(node, p)
  when node oftype FunctionInvocation.class then quoteFunctionInvocation(node, p)
  when node oftype ReturnStatement.class then quoteReturn(node, p)
  when node oftype ThrowStatement.class then quoteThrow(node, p)
  when node oftype AssignmentStatement.class then quoteAssignment(node, p)
  when node oftype ReferenceLookup.class then quoteReference(node, p)
  when node oftype CollectionLiteral.class then quoteCollection(node, p)
  when node oftype ModuleImport.class then quoteModuleImport(node, p)
  when node oftype NamedArgument.class then quoteNamedArgument(node, p)
  when node oftype LocalReference.class then quoteLocalRef(node, p)
  when node oftype MethodInvocation.class then quoteMethodInvocation(node, p)
  when node oftype TryCatchFinally.class then quoteTryCatch(node, p)
  when node oftype Struct.class then quoteStruct(node, p)
  when node oftype Member.class then quoteMember(node, p)
  when node oftype Union.class then quoteUnion(node, p)
  when node oftype LoopBreakFlowStatement.class then quoteLoopBreak(node, p)
  when node oftype LoopStatement.class then quoteLoopStatement(node, p)
  when node oftype ForEachLoopStatement.class then quoteForEachLoop(node, p)
  when node oftype MatchExpression.class then quoteWhenClauses(callIr("MatchExpression.match"), node, p)
  when node oftype CaseStatement.class then quoteWhenClauses(callIr("CaseStatement.cases"), node, p)
  when node oftype ConditionalBranching.class then quoteConditional(node, p)
  when node oftype ClosureReference.class then quoteClosure(node, p)
  when node oftype GoloFunction.class then quoteFunction(node, p)
  when node oftype Decorator.class then quoteDecorator(node, p)
  when node oftype Augmentation.class then quoteAugmentation(node, p)
  when node oftype NamedAugmentation.class then quoteNamedAugmentation(node, p)
  when node oftype DestructuringAssignment.class then quoteDestruct(node, p)
  when node oftype CollectionComprehension.class then quoteCollComprehension(node, p)
  when node oftype Noop.class then callIR("Noop.of"): withArgs(
      ConstantStatement.of(node: comment()))
  otherwise raise(String.format("Can't quote `%s`", node))
}

----
Checks if the node is unquoted.
----
function isUnquoted = |node| -> node: metadata("unquoted") orIfNull false

----
Wrapper function to make quote work on collections of node, blocks, and
toplevels
----
local function _quote_ = |protected, nodes| ->  match {
  when nodes: isEmpty() then ConstantStatement.of(null)
  when nodes: size() == 1 then quoteNode(nodes: get(0), protected)
  when nodes: get(0) oftype ToplevelGoloElement.class then
      callIr("ToplevelElements.of"): withArgs(quoteNodeArray(nodes, protected))
  otherwise callIr("Block.block"): withArgs(quoteNodeArray(nodes, protected))
}

----
Converts the given nodes to calls to builder functions.

This macro should "just work". If applied to a single statement, a single node is returned.
When applied to several statements, a block is returned, unless the statements are top-level ones, in which case a
`ToplevelElements` is returned.

See [`quoteNode`](#quoteNode_2).
----
@contextual
macro quote = |self, nodes...| {
  SYMBOLS: value(): enter(manglePrefix(self))
  let quoted = _quote_(set[], nodes)
  SYMBOLS: value(): exit()
  return quoted
}

----
Generate a mangle prefix from the macro call parameters.

This helps to make unique names when calling the same macro several times.
----
local function manglePrefix = |invocation| {
  let mod = invocation: enclosingModule()
  let fun = invocation: ancestorOfType(GoloFunction.class)
  return "%s_%s_%d": format(
    mod: packageAndClass(): toString(),
    fun?: name() orIfNull "",
    fun?: arity() orIfNull 0)
}
----
Converts the given nodes to calls to builder functions.

Contrary to [`quote`](#quote_1v), no name is mangled by this macro.

See [`quoteNode`](#quoteNode_2).
----
macro quoteNoMangle = |nodes...| -> _quote_(null, nodes)

----
Marks the node as unquoted so that it will be returned unchanged by the
[`quote`](#quote_1v) macro.
----
macro unquote = |node| -> node: metadata("unquoted", true)

----
Alias for [`unquote`](#unquote_1)
----
macro $ = |node| -> node: metadata("unquoted", true)

