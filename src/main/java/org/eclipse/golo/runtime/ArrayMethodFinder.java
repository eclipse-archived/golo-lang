/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

class ArrayMethodFinder implements MethodFinder {

  private final Class<?> receiverClass;
  private final Object[] args;
  private final MethodType type;
  private final int arity;
  private final String name;
  private final Lookup lookup;

  public ArrayMethodFinder(MethodInvocationSupport.InlineCache inlineCache, Class<?> receiverClass, Object[] args) {
    this.args = args;
    this.receiverClass = receiverClass;
    this.type = inlineCache.type();
    this.arity = args.length - 1;
    this.name = inlineCache.name;
    this.lookup = inlineCache.callerLookup;
  }

  private void checkArity(int value) {
    if (arity != value) {
      throw new UnsupportedOperationException(name + " on arrays takes "
          + (value == 0 ? "no" : value)
          + " parameter" + (value > 1 ? "s" : "")
          );
    }
  }

  @Override
  public MethodHandle find() {
    try {
      return resolve().asType(type);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private MethodHandle resolve() throws NoSuchMethodException, IllegalAccessException {
    switch (name) {
      case "get":
        checkArity(1);
        return MethodHandles.arrayElementGetter(receiverClass);
      case "set":
        checkArity(2);
        return MethodHandles.arrayElementSetter(receiverClass);
      case "size":
      case "length":
        checkArity(0);
        return lookup.findStatic(Array.class, "getLength", methodType(int.class, Object.class));
      case "iterator":
        checkArity(0);
        return lookup.findConstructor(PrimitiveArrayIterator.class, methodType(void.class, Object[].class));
      case "toString":
        checkArity(0);
        return lookup.findStatic(Arrays.class, "toString", methodType(String.class, Object[].class));
      case "asList":
        checkArity(0);
        return lookup.findStatic(
            Arrays.class, "asList", methodType(List.class, Object[].class))
          .asFixedArity();
      case "equals":
        checkArity(1);
        return lookup.findStatic(Arrays.class, "equals", methodType(boolean.class, Object[].class, Object[].class));
      case "getClass":
        checkArity(0);
        return MethodHandles.dropArguments(MethodHandles.constant(Class.class, receiverClass), 0, receiverClass);
      case "head":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "head", methodType(Object.class, Object[].class))
          .asType(type);
      case "tail":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "tail", methodType(Object[].class, Object[].class))
          .asType(type);
      case "isEmpty":
        checkArity(0);
        return lookup.findStatic(
            ArrayHelper.class, "isEmpty", methodType(boolean.class, Object[].class))
          .asType(type);
      default:
        throw new UnsupportedOperationException(name + " is not supported on arrays");
    }
  }
}

