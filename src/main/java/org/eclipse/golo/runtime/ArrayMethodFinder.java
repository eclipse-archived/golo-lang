/*
 * Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

import java.lang.invoke.*;
import java.lang.reflect.Array;
import java.util.*;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

import static gololang.Messages.message;

class ArrayMethodFinder extends MethodFinder {

  ArrayMethodFinder(MethodInvocation invocation, Lookup lookup) {
    super(invocation, lookup);
  }

  private void checkArity(int value) {
    if (invocation.arity() != value + 1) {
      throw new UnsupportedOperationException(
          message("array_method_bad_arity", invocation.name(), value));
    }
  }

  @Override
  public MethodHandle find() {
    try {
      return invocation.coerce(resolve());
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private MethodHandle resolve() throws NoSuchMethodException, IllegalAccessException {
    switch (invocation.name()) {
      case "get":
        checkArity(1);
        return MethodHandles.arrayElementGetter(invocation.receiverClass());
      case "set":
        checkArity(2);
        return MethodHandles.arrayElementSetter(invocation.receiverClass());
      case "size":
      case "length":
        checkArity(0);
        return lookup.findStatic(Array.class, "getLength", methodType(int.class, Object.class));
      case "iterator":
        checkArity(0);
        return lookup.findConstructor(PrimitiveArrayIterator.class,
                                      methodType(void.class, Object[].class));
      case "toString":
        checkArity(0);
        return lookup.findStatic(Arrays.class, "toString", methodType(String.class, Object[].class));
      case "asList":
        checkArity(0);
        return lookup.findStatic(Arrays.class, "asList", methodType(List.class, Object[].class));
      case "toArray":
        checkArity(0);
        return MethodHandles.identity(invocation.receiverClass());
      case "destruct":
        checkArity(0);
        return lookup.findStatic(gololang.Tuple.class, "fromArray", methodType(gololang.Tuple.class, Object[].class));
      case "__$$_destruct":
        checkArity(2);
        return lookup.findStatic(
            ArrayHelper.class, "newStyleDestruct", methodType(gololang.Tuple.class, Object[].class, int.class, boolean.class));
      case "equals":
        checkArity(1);
        return lookup.findStatic(Arrays.class, "equals",
                                 methodType(boolean.class, Object[].class, Object[].class));
      case "getClass":
        checkArity(0);
        return MethodHandles.dropArguments(
            MethodHandles.constant(Class.class, invocation.receiverClass()),
            0, invocation.receiverClass());
      case "head":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "head", methodType(Object.class, Object[].class));
      case "first":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "first", methodType(Object.class, Object[].class));
      case "last":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "last", methodType(Object.class, Object[].class));
      case "tail":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "tail", methodType(Object[].class, Object[].class));
      case "isEmpty":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "isEmpty", methodType(boolean.class, Object[].class));
      case "contains":
        checkArity(1);
        return lookup.findStatic(
            ArrayHelper.class, "contains", methodType(boolean.class, Object[].class, Object.class));
      case "indexOf":
        checkArity(1);
        return lookup.findStatic(
            ArrayHelper.class, "indexOf", methodType(int.class, Object[].class, Object.class));
      default:
        throw new UnsupportedOperationException(message("array_method_not_supported", invocation.name()));
    }
  }
}

