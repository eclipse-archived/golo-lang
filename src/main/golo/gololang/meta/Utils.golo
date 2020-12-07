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
Utilities to deal with meta-programming.
----
module gololang.meta.Utils

----
Adds metadata to an element.

The metadata can be used by another macro, for instance to conditionally deal with the marked element.

The metadata value must be a value convertible by
[`gololang.macros.Utils::getLiteralValue`](../macros/Utils.html#getLiteralValue_1)

- *param* `key`: the metadata key (as a String)
- *param* `value`: the metadata value
- *param* `element`: the element to which add the metadata
- *returns* the element itself, with metadata added.
----
macro withMetadata = |key, value, element| -> element: metadata(
  key: value(),
  gololang.macros.Utils.getLiteralValue(value))



augment gololang.ir.GoloElement {

  ----
  Dynamic metadata.

  This fallback methods dispatch to getting or setting the corresponding metadata on the receving element, depending on
  the number of given arguments.

  For instance, `elt: foobar(42)` will dispatch to `elt: metadata("foobar", 42)`,
  provided that the `elt` element does not have a `foobar` method.
  Conversely, `elt: foobar()` will dispatch to `elt: inheritedMetadata("foobar")`.

  A Golo element have thus a behaviour similar to a `DynamicObject` with respect to its metadata.

  See also [`GoloElement::metadata`](../../../javadoc/gololang/ir/GoloElement.html#metadata-java.lang.String-java.lang.Object-)
  and [`GoloElement::inheritedMetadata`](../../../javadoc/gololang/ir/GoloElement.html#inheritedMetadata-java.lang.String-)
  ----
  function fallback = |this, name, args...| {
    if args: isEmpty() {
      return this: inheritedMetadata(name)
    }
    if args: size() == 1 {
      return this: metadata(name, args: get(0))
    }
    return this: metadata(name, args)
  }
}
