# ............................................................................................... #
#
# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA-Lyon) and others
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
#
# ............................................................................................... #
----
This module defines the set of predefined macros. It is `&use`d by default.
----
module gololang.macros

import gololang.ir
import gololang.macros.Utils

----
Don't expand the result of a macro.

This special macro configure the expansion process to not expand the macros
contained in the result of a macro expansion.
This can be useful for debugging, to diagnose intermediary steps.
----
@special
macro dontRecurse = |visitor| {
  visitor: recurse(false)
}


----
Don't try to expand regular calls as macros.

This special macro configure the expansion process to not be tried on regular
function invocations. Only explicit macro calls (prefixed with `&`) are
expanded.
----
@special
macro dontExpandRegularCalls = |visitor| {
  visitor: expandRegularCalls(false)
}


----
Define the recursion limit for macro expansion in the current module.

This setting only takes effect for macros following it.
----
@special
macro recursionLimit = |visitor, limit| {
  require(limit oftype gololang.ir.ConstantStatement.class
          and limit: value() oftype java.lang.Integer.class,
      "The recursion limit must be an integer")
  visitor: recursionLimit(limit: value())
}

----
Adds a module in the macro resolution scope.

Modules added with this special macro are used for macro resolution, but are not
imported. They does not appear in the compiled code, and are not used for
regular function invocation resolutions.

Moreover, if the used module has a macro named `init`, a call to this macro,
using the additional parameters of `use`, is injected into the module.

For instance:

```golo
module Test

&use("my.macros", "answer", 42)
```

configures the visitor to lookup macros in the `my.macros` module, and expands
to

```golo
module Test

&my.macros.init("answer", 42)
```

The `init` macro may be contextual, to further modify the calling module.

- *param* `visitor`: the `MacroExpansionIrVisitor` used to expand this macro (injected)
- *param* `mod`: the name of the module to use as a string, or a class literal.
- *param `args`: additional arguments that will be passed to `init`
- *returns* the call to `init` if the macro exists, `null` otherwise.
----
@special
macro use = |visitor, mod, args...| {
  require(mod oftype gololang.ir.ConstantStatement.class, "argument to `use` must be a constant")
  let value = mod: value()
  var name = match {
    when value oftype java.lang.String.class then value
    when value oftype gololang.ir.ClassReference.class then value: name()
    otherwise raise("argument to `use` must be a string or a class literal")
  }
  visitor: useMacroModule(name)
  let init = gololang.ir.MacroInvocation.create(name + ".init", args)
  if visitor: macroExists(init) {
    return init
  }
}

----
Macro to mark an element as deprecated.

Can be used as a toplevel macro decorator.
For instance, to mark a function as deprecated:

```golo
@deprecated
function foo = |a, b| -> a + b
```

Moreover, some additional informations can be provided. They are used to modify the element documentation.

```golo
@deprecated(since="3.4", comment="Use `gololang.Functions::add` instead")
function foo = |a, b| -> a + b
```

- *param* `since`: the version since the element is deprecated
- *param* `comment`: a comment explaining the reasons of the deprecation or the element to use instead
- *returns* the element itself

See also [`gololang.meta.Annotations::makeDeprecated`](meta/Annotations.html#makeDeprecated_3v)
----
macro deprecated = |args...| {
  # TODO: generate a list of deprecated functions and types (a la javadoc) ?
  # TODO: when swithching to java >= 9, adds the `since` and `forRemoval` arguments to the annotation
  let positional, named, _ = parseArguments(args)
  require(positional: size() > 0, "`deprecated` macro must be applied on an element")
  return gololang.meta.Annotations.makeDeprecated(
      named: get("since")?: value(),
      named: get("comment")?: value(),
      positional)
}

----
Use old-style destructuring for the current module.

This macro customize the behavior of the destructuring feature by forcing the use of the `destruct` method instead of
`_$$_destruct`.

This is a toplevel macro.
----
@contextual
macro useOldstyleDestruct = |self| {
  self: enclosingModule(): metadata("golo.destruct.newstyle", false)
}

----
Adds metadata to an element.

The metadata can be used by another macro, for instance to conditionally deal with the marked element.

The metadata value must be a value convertible by
[`gololang.macros.Utils::getLiteralValue`](../macros/Utils.html#getLiteralValue_1)

This macro is called with named parameters for metadata followed with the element to annotate, as in:

```golo
@meta(foo="bar", answer=42)
function example = -> null
```

It is similar to [`withMetadata`](#withMetadata_3), but with a more convenient syntax, and allows to add several
metadata at once.

- *param* `key=value` sequence
- *param* `element`: the element to which add the metadata
- *returns* the element itself, with metadata added.

See also [`withMetadata`](#withMetadata_3)
----
macro meta = |args...| {
  let _, n, elt = parseArguments(args, true)
  foreach k, v in n: entrySet() {
    elt: metadata(k, getLiteralValue(v))
  }
  return elt
}

----
Adds metadata to an element.

The metadata can be used by another macro, for instance to conditionally deal with the marked element.

The metadata value must be a value convertible by
[`gololang.macros.Utils::getLiteralValue`](../macros/Utils.html#getLiteralValue_1)

This macro is similar to [`meta`](#meta_1v), but allows to use a key with a name not allowed by the named parameter
syntax (for instance `"foo.bar Bz"`.

- *param* `key`: the metadata key (as a String)
- *param* `value`: the metadata value
- *param* `element`: the element to which add the metadata
- *returns* the element itself, with metadata added.

See also [`meta`](#meta_1v)
----
macro withMetadata = |key, value, element| -> element: metadata(key: value(), getLiteralValue(value))


