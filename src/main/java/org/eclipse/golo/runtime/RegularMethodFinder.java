/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import gololang.GoloStruct;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.reflect.Modifier.*;
import static java.util.stream.Collectors.toList;

class RegularMethodFinder extends MethodFinder {

  private final boolean makeAccessible;
  private boolean isOverloaded;

  RegularMethodFinder(MethodInvocation invocation, Lookup lookup) {
    super(invocation, lookup);
    this.makeAccessible = !isPublic(invocation.receiverClass().getModifiers());
  }

  @Override
  public MethodHandle find() {
    final List<MethodHandle> candidates = Stream.concat(
        findInMethods().map(this::toMethodHandle),
        findInFields().map(this::toMethodHandle))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toList());
    if (candidates.isEmpty()) {
      return null;
    }
    isOverloaded = candidates.size() > 1;
    return candidates.get(0);
  }

  public boolean isOverloaded() {
    return isOverloaded;
  }

  private Optional<MethodHandle> toMethodHandle(Field field) {
    if (makeAccessible) {
      field.setAccessible(true);
    }
    try {
      if (invocation.arity() == 1) {
        return Optional.of(lookup.unreflectGetter(field).asType(invocation.type()));
      } else {
        return Optional.of(
            filterReturnValue(
                lookup.unreflectSetter(field),
                constant(invocation.receiverClass(), invocation.arguments()[0]))
                .asType(invocation.type()));
      }
    } catch (IllegalAccessException e) {
      /* We need to give augmentations a chance, as IllegalAccessException can be noise in our resolution.
       * Example: augmenting HashSet with a map function.
       *  java.lang.IllegalAccessException: member is private: java.util.HashSet.map/java.util.HashMap/putField
       */
      return Optional.empty();
    }
  }

  @Override
  protected Optional<MethodHandle> toMethodHandle(Method method) {
    if (makeAccessible || isValidPrivateStructAccess(method)) {
      method.setAccessible(true);
    }
    return super.toMethodHandle(method).map(
        handle -> FunctionCallSupport.insertSAMFilter(handle, lookup, method.getParameterTypes(), 1));
  }

  private boolean isValidPrivateStructAccess(Method method) {
    Object receiver = invocation.arguments()[0];
    if (!(receiver instanceof GoloStruct)) {
      return false;
    }
    String receiverClassName = receiver.getClass().getName();
    String callerClassName = callerClass.getName();
    return method.getName().equals(invocation.name())
        && isPrivate(method.getModifiers())
        && (receiverClassName.startsWith(callerClassName)
        || callerClassName.equals(reverseStructAugmentation(receiverClassName)))
        && TypeMatching.argumentsMatch(method, invocation.arguments());
  }

  private static String reverseStructAugmentation(String receiverClassName) {
    return receiverClassName.substring(0, receiverClassName.indexOf(".types"))
        + "$" + receiverClassName.replace('.', '$');
  }


  private Stream<Method> findInMethods() {
    return Extractors.getMethods(invocation.receiverClass())
        .filter(m -> invocation.match(m) || isValidPrivateStructAccess(m));
  }

  private Stream<Field> findInFields() {
    if (invocation.arity() > 3) {
      return Stream.empty();
    }
    return Extractors.getFields(invocation.receiverClass())
        .filter(this::isMatchingField);
  }

  private boolean isMatchingField(Field field) {
    return field.getName().equals(invocation.name()) && !isStatic(field.getModifiers());
  }
}
