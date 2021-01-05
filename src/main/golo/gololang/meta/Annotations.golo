# ............................................................................................... #
#
# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
#
# ............................................................................................... #
----
Module to deal with java annotations.

To flag a golo element with a Java annotation, one must add some metadata to the IR element.
The metadata will be used by the compiler to add the required bytecode.

To add such metadata, the most direct way is to create a macro that modify the IR node.

This module provides functions to ease the creation of such macros by:

- adding the annotation metadata ([`annotateElements`](#annotateElements_3v))
- parsing the macro arguments and match them with the annotation fields ([`extractAnnotationArguments`](#extractAnnotationArguments_2))
- checking that the target has a valid type w.r.t. the annotation definition ([`checkApplicableTo`](#checkApplicableTo_2))

Moreover, since the creation of simple macro that only adds the metadata to the element is just boilerplate code,
macros to generate such macros are provided (
[`annotationWrapper`](#annotationWrapper_1),
[`annotationWrapper`](#annotationWrapper_2) and
[`annotationWrapper`](#annotationWrapper_3))

Finally, a helper function to mark a element as deprecated is also provided.
----
module gololang.meta.Annotations

import gololang.ir
import gololang.ir.DSL
import gololang.macros.Utils

----
Adds an annotation metadata to an element.

This is a low-level function, no check is done.

See [`annotateElements`](#annotateElements_3v) for a higher level function.

- *param* `annotationClass`: the class of the annotation to add
- *param* `args`: a map representing the annotation arguments
- *param* `target`: the element to annotate
- *returns* the element itself

See [`extractAnnotationArguments`](#extractAnnotationArguments_2).
----
function addAnnotationMetadata = |annotationClass, args, target| {
  if target: metadata("annotations") is null {
    target: metadata("annotations", set[])
  }
  target: metadata("annotations"): add([
    org.eclipse.golo.compiler.PackageAndClass.of(annotationClass: name()): toJVMRef(),
    annotationClass: getAnnotation(java.lang.annotation.Retention.class)?: value() is java.lang.annotation.RetentionPolicy.RUNTIME(),
    args])
  return target
}

----
Mark the element as deprecated, by setting the `deprecated` metadata and adding
the `java.lang.Deprecated` annotation.
Indeed, the annotation by itself is not sufficient, since a bytecode flag must also be set for the element to be
considered deprecated by the Java compiler. The bytecode generator use the `deprecated` metadata to set this flag.

Moreover, the documentation of the concerned element is also updated to document the deprecation.

Can be applied to function declarations, types, or augmentations.
Otherwise, the element is returned unchanged.

- *param* `since`: the version in which the element became deprecated.
- *param* `comment`: a comment to append to the element documentation.
- *param* `elts`: the element to mark as deprecated.
- *returns* the elements, wrapped in a `ToplevelElements` if required

This function should not need to be called directly.
Use the [`gololang.macros::deprecated`](../macros.html#deprecated_1v) macro instead
----
function makeDeprecated = |since, comment, elts...|  {
  # TODO: when using java >= 9, add since and removal arguments to the annotation
  let elt = wrapToplevel(elts)
  elt: accept(Visitors.visitor!(|v,a,e| {
    if e: isOneOf(GoloFunction.class, GoloType.class, GoloModule.class) {
      annotateElement(e, java.lang.Deprecated.class, null)
      e: metadata("deprecated", true)
      e: documentation(generateDeprecatedDocumentation(e: documentation(), since, comment))
    }
    if e: isOneOf(GoloModule.class, NamedAugmentation.class, Augmentation.class, ToplevelElements.class) {
      e: walk(v)
    }
  }, Visitors$Walk.NONE()))
  return elt
}

local function generateDeprecatedDocumentation = |eltDoc, since, comment| {
  let doc = StringBuilder(eltDoc orIfNull "")
  doc: append("\n\n*Deprecated*")
  if since isnt null {
    doc: append(" since "): append(since): append(".")
  }
  if comment isnt null {
    doc: append(" "): append(comment)
  }
  return doc: toString()
}

----
Macro to generate a macro to add a Java annotation to a golo element.

The name of the macro is the simple name of the annotation class, its documentation is auto-generated.

- *param* `annotation`: the litteral fully qualified class reference to the annotation to use.
- *returns* a macro that will add the given annotation to a golo element

See [`annotationWrapper`](#annotationWrapper_3)
----
macro annotationWrapper = |annotation| -> generateWrapper(
  annotation: value(): getName(): split("\\."): last(),
  annotation)
  : documentation(generateDocForWrapper(annotation: value()))

----
Macro to generate a macro to add a Java annotation to a golo element.

The name of the generated macro is given and its documentation is auto-generated.

- *param* `annotation`: the litteral fully qualified class reference to the annotation to use.
- *param* `name`: the name of the generated macro (as a reference lookup, i.e. a name, not a string).
- *returns* a macro that will add the given annotation to a golo element

See [`annotationWrapper`](#annotationWrapper_3)
----
macro annotationWrapper = |annotation, name| -> generateWrapper(
  name: value(),
  annotation)
  : documentation(generateDocForWrapper(annotation: value()))

----
Macro to generate a macro to add a Java annotation to a golo element.

For instance, to create a macro to apply the `mypackage.MyAnnotation` Java annotation, one must create a macro such as:

    ---
    Apply the `MyAnnotation` annotation
    ---
    macro annotation = |args...| {
      let a, u, e = extractAnnotationArguments(mypackage.MyAnnotation.class, args)
      return annotateElements(mypackage.MyAnnotation.class, a, e)
    }

used as

```golo
@annotation(prop1="hello", prop2=42)
function foo = -> null
```
(given the annotation has the `prop1` and `prop2` properties.

This macro generate the corresponding macro, thus allowing to simply write

```golo
&annotationWrapper(
    mypackage.MyAnnotation.class,
    annotation,
    "Apply the `MyAnnotation` annotation")
```

- *param* `annotation`: the litteral fully qualified class reference to the annotation to use.
- *param* `name`: the name of the generated macro (as a reference lookup, i.e. a name, not a string).
- *param* `doc`: the litteral string of the documentation for the generated macro.
- *returns* a macro that will add the given annotation to a golo element

See also [`extractAnnotationArguments`](#extractAnnotationArguments_2) and [`annotateElements`](#annotateElements_3v)
----
macro annotationWrapper = |annotation, name, doc| -> generateWrapper(
  name: value(), annotation): documentation(doc: value())

local function generateWrapper = |macroName, annotationClassReference| -> `macro(macroName)
  : withParameters("args"): varargs()
  : body(
    `let(["a", "u", "e"], call("gololang.meta.Annotations.extractAnnotationArguments"): withArgs(
        annotationClassReference, refLookup("args"))),
    `return(call("gololang.meta.Annotations.annotateElements"): withArgs(
        annotationClassReference, refLookup("a"), refLookup("e")))
  )

local function generateDocForWrapper = |ref| {
  let doc = StringBuilder("Generated macro to apply the `"):append(ref: name()):append("` java annotation to golo elements.")
            : append(System.lineSeparator())
  var annotationClass = null
  try {
    foreach fieldName, type, default in extractAnnotationFields(ref: dereference()) {
      doc: append(System.lineSeparator()): append("- *param* `"): append(fieldName): append("`")
      if default isnt null {
        doc: append(": default "): append(default)
      }
    }
  } catch (e) {
    if e oftype ClassNotFoundException.class {
      Messages.warning("Annotation class %s not found": format(ref: name()))
    } else {
      throw e
    }
  }
  doc: append(System.lineSeparator())
  : append("- *returns* the annotated element wrapped in a `ToplevelElements` if required")
  return doc: toString()
}

----
Adds an annotation to the given elements.

This function correctly deal with several elements by returning a `ToplevelElements`, and can therefore be used directly
by a macro.

The properties of the annotation are given in a map as returned by [`extractAnnotationArguments`](#extractAnnotationArguments_2).
For instance, to have the equivalent of the Java code:
```java
@mypackage.MyAnnotation(prop1="hello", prop2=42)
public static Object foo() {
  return null;
}
```
one should annotate the `foo` function definition IR with:
```golo
annotateElements(
    mypackage.MyAnnotation.class,
    map[
      ["prop1", "hello"],
      ["prop2", 42]
    ],
    theFooIR)
```

- *param* `annotationClass`: The class of the annotation.
- *param* `args`: a map of the properties of the annotation.
- *param* `elts`: the IR elements to annotate.
- *returns* the annotated element itself if there is only one, or a `ToplevelElements` if there are several.

See [`annotationWrapper`](#annotationWrapper_3), [`extractAnnotationArguments`](#extractAnnotationArguments_2)
----
function annotateElements = |annotationClass, args, elts...| -> annotateElement(
  wrapToplevel(elts), annotationClass, args)

local function annotateElement = |elt, annotationClass, args| {
  if elt oftype gololang.ir.ToplevelElements.class {
    foreach e in elt {
      annotateElement(e, annotationClass, args)
    }
  } else {
    checkApplicableTo(annotationClass, elt)
    addAnnotationMetadata(annotationClass, args, elt)
  }
  return elt
}

# TODO: mapping for other types: fields, variables
local function isApplicableTo = |annotationClass, target| {
  let annotationTargets = annotationClass: getAnnotation(java.lang.annotation.Target.class)?: value()
  let targetType = match {
    when target oftype gololang.ir.GoloFunction.class then java.lang.annotation.ElementType.METHOD()
    when target oftype gololang.ir.GoloType.class then java.lang.annotation.ElementType.TYPE()
    when target oftype gololang.ir.GoloModule.class then java.lang.annotation.ElementType.TYPE()
    when target oftype gololang.ir.NamedAugmentation.class then java.lang.annotation.ElementType.TYPE()
    when target oftype gololang.ir.Augmentation.class and target: hasFunctions() then java.lang.annotation.ElementType.TYPE()
    otherwise null
  }
  return annotationTargets is null
         or (targetType isnt null
             and java.util.Arrays.asList(annotationTargets): contains(targetType))
}


----
Checks if a Java annotation can be applied to a golo element.

Inspect the annotation's `Target` annotation.

- *param* `annotationClass`: the class of the Java annotation
- *param* `target`: the golo IR element to annotate
- *throws* an exception if the element can't be annotated.
----
function checkApplicableTo = |annotationClass, target| {
  require(annotationClass: getAnnotation(java.lang.annotation.Retention.class)?:value()
          isnt java.lang.annotation.RetentionPolicy.SOURCE(),
    "Golo can't deal with SOURCE retention annotations")
  require(isApplicableTo(annotationClass, target),
          "Annotation %s not applicable on a %s": format(annotationClass: getName(), target: getClass(): getName()))
}

local function extractAnnotationFields =|annotationClass| {
  return array[
    [
      meth: getName(),
      org.eclipse.golo.runtime.TypeMatching.boxed(meth: getReturnType()),
      meth: getDefaultValue()
    ] foreach meth in annotationClass: getDeclaredMethods()
  ]
}

local function extractAnnotationNamedArguments = |annotClass, namedArgs| {
  let annotationFields = map[]
  foreach field in extractAnnotationFields(annotClass) {
    fillArgumentsMap(annotationFields, field, getLiteralValue(namedArgs: get(field: get(0))))
    namedArgs: remove(field: get(0))
  }
  let unknown = map[]
  foreach k, v in namedArgs: entrySet() {
    unknown: put(k, getLiteralValue(v))
  }
  return [annotationFields, unknown]
}

local function filterPositionnalArguments = |args| {
  let arguments = vector[]
  let elements = vector[]
  foreach arg in args {
    let value = getLiteralValue(arg)
    if value is null {
      continue
    }
    if value oftype gololang.ir.GoloElement.class {
      elements: add(value)
    } else {
      arguments: add(value)
    }
  }
  require(not (elements: isEmpty()), "No element to annotate")
  return [arguments, elements: toArray()]
}

local function fillArgumentsMap = |map, field, value| {
  let name, t, d = field
  if (value isnt null) {
    map: put(name, value)
  }
}

----
Parse the macro arguments according to an annotation class.

The named arguments of the macro are matched with the annotation fields.
If the annotation has at most one field, positional argument can be used.
The named is extracted using reflection on the annotation class.
The annotation fields are returned in a map of the name and values

- *param* `annotation`: the annotation class
- *param* `args`: the full array of arguments of the macro
- *returns* a tuple containing a map of named arguments,
            a map of named arguments not recognized by the annotation,
            and an array of elements to annotate

See [`parseArguments`](../macros/Utils.html#parseArguments_1),
    [`getLiteralValue`](../macros/Utils.html#getLiteralValue_1)
----
function extractAnnotationArguments = |annotation, args| {
  let p, n, _ = parseArguments(args)
  let namedMap, unknown = extractAnnotationNamedArguments(annotation, n)
  let positionnals, elements = filterPositionnalArguments(p)
  let fields = extractAnnotationFields(annotation)
  require(not (fields: isEmpty())
          or (namedMap: isEmpty() and positionnals: isEmpty()),
      "The annotation %s has no field": format(annotation: name()))
  require(fields: size() <= 1 or positionnals: isEmpty(),
      "The annotation %s has several fields. Use the named arguments syntax": format(annotation: name()))
  require((fields: size() != 1 or fields: get(0): get(2) isnt null) or not (namedMap: isEmpty()) or not (positionnals: isEmpty()),
      "The annotation %s has 1 field with no default and no argument was provided": format(annotation: name()))

  if fields: size() == 1 and namedMap: isEmpty() and not (positionnals: isEmpty()) {
    fillArgumentsMap(namedMap, fields: get(0), positionnals: get(0))
  }
  return [namedMap, unknown, elements]
}

