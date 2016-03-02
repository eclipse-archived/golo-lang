/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.invoke.*;
import java.lang.reflect.Array;
import java.util.*;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

class ArrayMethodFinder extends MethodFinder {

  ArrayMethodFinder(MethodInvocation invocation, Lookup lookup) {
    super(invocation, lookup);
  }

  private void checkArity(int value) {
    if (invocation.arity() != value + 1) {
      throw new UnsupportedOperationException(invocation.name() + " on arrays takes "
          + (value == 0 ? "no" : value)
          + " parameter" + (value > 1 ? "s" : "")
          );
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
      case "tail":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "tail", methodType(Object[].class, Object[].class));
      case "isEmpty":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "isEmpty", methodType(boolean.class, Object[].class));
      default:
        throw new UnsupportedOperationException(invocation.name() + " is not supported on arrays");
    }
  }
}

