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
This module contains some functions to help writing macros.
----
module gololang.macros.Utils

import gololang.ir
import gololang.ir.DSL

----
Utility to extract the last argument of a varargs function.

Useful when creating a macro used as a decorator that can take multiple
arguments.

# Example

Such a macro can be used as

```golo
@myMacro(answer=42, ping="pong")
function myFunction = ...
```

It will be defined as
```golo
macro foo = |args...| {
  let arguments, element = extractLastArgument(args)
  #...
  return element
}
```

In this example, `arguments` will be an array of `NamedArgument`s, and `element`
will contain the `GoloFunction`.

- *param* `args`: an array containing the arguments
- *returns* a tuple containing an array of the arguments but the last, and the
  last argument.

See also [`parseArguments`](#parseArguments_2)
----
function extractLastArgument = |args| -> [
  java.util.Arrays.copyOf(args, args: length() - 1),
  args: get(args: length() - 1)
]

----
Converts a `NamedArgument` collection into a map whose keys are the names of the
named arguments, and values the associated values.

# Example

Given a macro defined as:
```golo
macro foo = |args...| {
  let arguments = namedArgsToMap(args)
  # ...
}
```

When called as `&foo(a=42, b="hello")`, the `arguments` variable will contains
`map[["a", constant(42)], ["b", constant("hello")]]`

The elements of the collection that are not `NamedArgument` are ignored.

See also [`parseArguments`](#parseArguments_2)
----
function namedArgsToMap = |args| -> map[
  [arg: name(), arg: expression()]
  foreach arg in args when arg oftype NamedArgument.class
]

----
Converts a collection of expressions and named arguments into a triple
containing a list of the expressions and a map extracted from named arguments
like by [`namedArgsToMap`](#namedArgsToMap_1).

If `extractLast` is `true` or `null`, also extract the last argument as by
[`extractLastArgument`](#extractLastArgument_1). If `extractLast` is a type, the last argument is extracted only if it
has the corresponding type.

# Example

```golo
macro myMacro = |args...| {
  let positional, named, last = parseArguments(args, true)
  # ...
}

@myMacro(answer=42, "foo", foo="bar")
function plop = -> "daplop"
```

In the `myMacro` macro, `positional` will be `array[constant("foo")]`, `named`
will be `map[["answer", constant(42)], ["foo", constant("bar")]]` and `last` will
be the `plop` function IR node.
----
function parseArguments = |args, extractLast| {
  if mustExtractLast(args, extractLast) {
    let begin, last = extractLastArgument(args)
    let e = parseArguments(begin)
    e: set(2, last)
    return e
  }
  return parseArguments(args)
}

local function mustExtractLast = |args, extractLast| -> match {
  when extractLast is null then true
  when extractLast oftype Class.class then (
    args: length() > 0 and (args: get(args: length() - 1) oftype extractLast))
  when extractLast oftype Boolean.class then extractLast
  otherwise false
}

----
Same as `parseArguments(args, false)`

See [`parseArguments(args, extractLast)`](#parseArguments_2)
----
function parseArguments = |args| -> array[
  array[arg foreach arg in args when not (arg oftype NamedArgument.class)],
  map[
    [arg: name(), arg: expression()]
    foreach arg in args when arg oftype NamedArgument.class
  ],
  null
]


----
Converts a IR node into the corresponding runtime value.

This only works for literal values, classes, enums or arrays of theses values.
Otherwise the node itself is returned.
----
function getLiteralValue = |node| -> match {
  when node is null then null
  when node oftype gololang.ir.ConstantStatement.class and node: value() oftype
    gololang.ir.ClassReference.class then node: value(): dereference()
  when node oftype gololang.ir.ConstantStatement.class then node: value()
  when node oftype gololang.ir.CollectionLiteral.class then array[getLiteralValue(e) foreach e in node: children()]
  when isEnum(node) then loadEnum(node: packageAndClass())
  otherwise node
}

local function isEnum = |arg| -> arg oftype gololang.ir.FunctionInvocation.class
                                and arg: arity() == 0
                                and Class.forName(arg: packageAndClass(): packageName()): isEnum()

local function loadEnum = |name| -> java.lang.Enum.valueOf(Class.forName(name: packageName()), name: className())


local function applyIfTypeMatches = |type, mac| -> |elt| -> match {
  when elt oftype type then mac(elt)
  otherwise elt
}

local function applyToAll = |fun, args| {
  let res = toplevels()
  foreach arg in args {
    if arg oftype ToplevelElements.class {
      res: add(applyToAll(fun, arg))
    } else {
      res: add(fun(arg))
    }
  }
  return res
}

----
Wrap the given object in a `ToplevelElements`.

If the argument is an array or a collection of more than 1 element, it is wrapped in a `ToplevelElements`.
Otherwise, the element is returned unchanged.
----
function wrapToplevel = |elt| -> match {
  when (isArray(elt) or elt oftype java.lang.Collection.class) and elt: size() == 1 then elt: get(0)
  when isArray(elt) or elt oftype java.lang.Collection.class then gololang.ir.ToplevelElements.of(elt)
  otherwise elt
}

----
Decorator to help define macros on top-level elements.

Macros applied on top-level elements may often return a `ToplevelElements` to
inject several top-level elements into the module, without using side effects.

When stacking such macros, for instance with the decorator notation, each macro
must be prepared to receive a `ToplevelElements` containing various kinds of
elements, instead of the decorated one.

For instance, a macro can work on a `struct` and inject several tooling
functions and augmentations, such as:

```golo
macro myMacro = |structure| -> toplevels(
  structure,
  `function("workOn" + structure: name())
      : withParameters("s"): body(...),
  `augment(structure): with(...)
)
```

Suppose that a macro `otherMacro` has a similar behavior. It is thus not possible to
stack these two macro, as in:
```golo
@otherMacro
@myMacro
struct Foo = {x}
```

since here, the `otherMacro` will not receive a `Struct`, but the
`ToplevelElements` returned by `myMacro`.

This decorator adapt the macro to deal with `ToplevelElements`, applying it to
each contained element if its type match the given one, and returning the
element unchanged otherwise.

In the previous example, we can use it on the two macros as:
```golo
@!toplevel(Struct.class)
macro myMacro = |structure| -> toplevels(
  structure,
  `function("workOn" + structure: name())
      : withParameters(s): body(...),
  `augment(structure: packageAndClass()): with(...)
)
```

The `myMacro` will be applied to its argument if its a `struct`, don't change
any other type, and if its argument is a `ToplevelElements`, it will be applied
to any contained element. The macro can therefore be stacked on the top of other
macros whatever their returned type.

To apply the macro on any type, just use `GoloElement.class` as a filter.

Moreover, the decorator makes the macro varargs. It can therefore be called on
several structures at once, like:

```golo
&myMacro {

struct Foo = {x}

struct Bar = {a, b}

}
```

This decorator should be banged.

- *param* `type`: the type of the nodes on which the macro must be applied.
----
function toplevel = |type| -> |mac| -> |args...| -> match {
  when args: size() == 1 and args: get(0) oftype type then mac(args: get(0))
  when args: size() == 1 and args: get(0) oftype ToplevelGoloElement.class then args: get(0)
  otherwise applyToAll(applyIfTypeMatches(type, mac), args)
}

#== Symbols and scope management =======================================

let SYMBOLS = org.eclipse.golo.compiler.SymbolGenerator()

----
Generates a new unique name using an internal `SymbolGenerator`.

See also [SymbolGenerator](../../../javadoc/org/eclipse/golo/compiler/SymbolGenerator.html)
----
function gensym = -> SYMBOLS: next()

----
Generates a new unique name using an internal `SymbolGenerator` with the given name as prefix.

See also [SymbolGenerator](../../../javadoc/org/eclipse/golo/compiler/SymbolGenerator.html)
----
function gensym = |name| -> SYMBOLS: next(name)

----
Mangles the given name to ensure hygiene.

If the argument is a `LocalReference` or a `ReferenceLookup`, a new object with
the same type, but with a mangled name, is returned.

See also [SymbolGenerator](../../../javadoc/org/eclipse/golo/compiler/SymbolGenerator.html)
----
function mangle = |name| -> match {
  when name oftype LocalReference.class then
    localRef(SYMBOLS: getFor(name: name()))
    : kind(name: kind())
    : synthetic(name: isSynthetic())
    : index(name: index())
  when name oftype ReferenceLookup.class then
    refLookup(SYMBOLS: getFor(name: name()))
  otherwise SYMBOLS: getFor(name: toString())
}

----
Enters a scope in the internal `SymbolGenerator` used by [`gensym`](#gensym_0) and [`mangle`](#mangle_1)
----
function enterSymScope = |scope| {
  SYMBOLS: enter(scope)
}

----
Exists a scope in the internal `SymbolGenerator` used by [`gensym`](#gensym_0) and [`mangle`](#mangle_1)
----
function exitSymScope = {
  SYMBOLS: exit()
}


#== Misc utilities =====================================================

----
Throws a `StopCompilationException` to stop the compilation process.

Note that this exception just stop the process and does not print anything on stderr.
You should deal with error messages yourself.

See `gololang.Messages` for instance to display console messages.
----
macro stopCompilation = |args...| -> `throw(call("org.eclipse.golo.compiler.StopCompilationException"): withArgs(args))


----
Generate a name in the current name space.

Useful if a macro generates calls to functions defined in the same module. Instead of relying on the module to be
imported, one wants to use a fully qualified function name. This macro generate such a name without hard coding the
current module name in the written macro.

For instance, instead of writing:
```
module MyModule

function myFunction = -> null

macro = -> gololang.ir.DSL.call("MyModule.myFunction")
```
we can write:
```
module MyModule

function myFunction = -> null

macro = -> gololang.ir.DSL.call(&gololang.macros.Utils.thisModule("myFunction")
```
----
@contextual
macro thisModule = |self, name| -> constant(self: enclosingModule(): packageAndClass(): toString() + "." + name: value())

----
Expands to the fully qualified name of the function in which this macro is called.

Can be used for instance to define variable scopes in macro.
----
@contextual
macro thisFunction = |self| -> constant(
    self: enclosingModule(): packageAndClass(): toString() + "$" + self: ancestorOfType(GoloFunction.class): getName())

