/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.copyOfRange;

public class PropertyMethodFinder extends MethodFinder {

  private static final MethodHandle FLUENT_SETTER;

  static {
    try {
      FLUENT_SETTER = lookup().findStatic(
          PropertyMethodFinder.class,
          "fluentSetter",
          methodType(Object.class, Object.class, Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required fluent method handles", e);
    }
  }

  private static Object fluentSetter(Object o, Object notUsedSetterReturnValue) {
    return o;
  }

  private String propertyName;

  public PropertyMethodFinder(MethodInvocation invocation, Lookup lookup) {
    super(invocation, lookup);
    this.propertyName = capitalize(invocation.name());
  }

  private static MethodInvocation propertyInvocation(String propertyInvocationName, MethodInvocation parentInvocation) {
    return new MethodInvocation(
        propertyInvocationName,
        parentInvocation.type(),
        parentInvocation.arguments(),
        copyOfRange(parentInvocation.argumentNames(), 0, parentInvocation.argumentNames().length - 1));
  }

  private MethodHandle findMethodForGetter() {
    MethodHandle target = new RegularMethodFinder(
        propertyInvocation("get" + propertyName, invocation),
        lookup
    ).find();

    if (target != null) {
      return target;
    }
    return new RegularMethodFinder(
        propertyInvocation("is" + propertyName, invocation),
        lookup
    ).find();
  }

  private MethodHandle fluentMethodHandle(Method candidate) {
    Objects.requireNonNull(candidate);
    MethodHandle target = toMethodHandle(candidate).orElse(null);
    if (target != null) {
      if (!TypeMatching.returnsValue(candidate)) {
        Object receiver = invocation.arguments()[0];
        MethodHandle fluent = FLUENT_SETTER.bindTo(receiver);
        target = filterReturnValue(target, fluent);
      }
    }
    return target;
  }

  private MethodHandle findMethodForSetter() {
    return new RegularMethodFinder(
        propertyInvocation("set" + propertyName, invocation),
        lookup)
        .findInMethods()
        .map(this::fluentMethodHandle)
        .findFirst()
        .orElse(null);
  }

  @Override
  public MethodHandle find() {
    if (invocation.arity() == 1) {
      return findMethodForGetter();
    }
    return findMethodForSetter();
  }

  private static String capitalize(String word) {
    return Character.toUpperCase(word.charAt(0)) + word.substring(1);
  }
}
