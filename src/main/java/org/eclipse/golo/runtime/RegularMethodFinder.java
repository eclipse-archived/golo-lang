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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import gololang.GoloStruct;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.copyOfRange;
import static org.eclipse.golo.runtime.DecoratorsHelper.getDecoratedMethodHandle;
import static org.eclipse.golo.runtime.DecoratorsHelper.isMethodDecorated;

class RegularMethodFinder implements MethodFinder {

  private final Object[] args;
  private final MethodType type;
  private final Class<?> receiverClass;
  private final String methodName;
  private final Lookup lookup;
  private final boolean makeAccessible;
  private final int arity;
  private final String[] argumentNames;

  public RegularMethodFinder(MethodInvocationSupport.InlineCache inlineCache, Class<?> receiverClass, Object[] args) {
    this.args = args;
    this.type = inlineCache.type();
    this.receiverClass = receiverClass;
    this.methodName = inlineCache.name;
    this.lookup = inlineCache.callerLookup;
    this.makeAccessible = !isPublic(receiverClass.getModifiers());
    this.arity = type.parameterArray().length;
    this.argumentNames = new String[inlineCache.argumentNames.length + 1];
    this.argumentNames[0] = "this";
    System.arraycopy(inlineCache.argumentNames,0, argumentNames, 1, inlineCache.argumentNames.length);
  }

  @Override
  public MethodHandle find() {
    try {
      MethodHandle target = findInMethods();
      if (target != null) { return target; }

      return findInFields();
    } catch (IllegalAccessException ignored) {
    /* We need to give augmentations a chance, as IllegalAccessException can be noise in our resolution.
     * Example: augmenting HashSet with a map function.
     *  java.lang.IllegalAccessException: member is private: java.util.HashSet.map/java.util.HashMap/putField
     */
      return null;
    }
  }

  private MethodHandle toMethodHandle(Field field) throws IllegalAccessException {
    MethodHandle target = null;
    if (makeAccessible) {
      field.setAccessible(true);
    }
    if (args.length == 1) {
      target = lookup.unreflectGetter(field).asType(type);
    } else {
      target = lookup.unreflectSetter(field);
      target = filterReturnValue(target, constant(receiverClass, args[0])).asType(type);
    }
    return target;
  }

  private MethodHandle toMethodHandle(Method method) throws IllegalAccessException {
    MethodHandle target = null;
    if (makeAccessible || isValidPrivateStructAccess(method)) {
      method.setAccessible(true);
    }
    if (isMethodDecorated(method)) {
      target = getDecoratedMethodHandle(method, arity);
    } else {
      if ((method.isVarArgs() && TypeMatching.isLastArgumentAnArray(type.parameterCount(), args))) {
        target = lookup.unreflect(method).asFixedArity().asType(type);
      } else {
        target = lookup.unreflect(method).asType(type);
      }
    }
    if(argumentNames.length > 1) {
      target = FunctionCallSupport.reorderArguments(method, target, argumentNames);
    }
    return FunctionCallSupport.insertSAMFilter(target, lookup, method.getParameterTypes(), 1);
  }

  private boolean isValidPrivateStructAccess(Method method) {
    Object receiver = args[0];
    if (!(receiver instanceof GoloStruct)) {
      return false;
    }
    String receiverClassName = receiver.getClass().getName();
    String callerClassName = lookup.lookupClass().getName();
    return method.getName().equals(methodName) &&
        isPrivate(methodModifiers()) &&
        (receiverClassName.startsWith(callerClassName) ||
            callerClassName.equals(reverseStructAugmentation(receiverClassName)));
  }

  private static String reverseStructAugmentation(String receiverClassName) {
    return receiverClassName.substring(0, receiverClassName.indexOf(".types")) + "$" + receiverClassName.replace('.', '$');
  }

  private List<Method> getCandidates() {
    List<Method> candidates = new LinkedList<>();
    HashSet<Method> methods = new HashSet<>();
    Collections.addAll(methods, receiverClass.getMethods());
    Collections.addAll(methods, receiverClass.getDeclaredMethods());
    for (Method method : methods) {
      if (isCandidateMethod(method)) {
        candidates.add(method);
      } else if (isValidPrivateStructAccess(method)) {
        candidates.add(method);
      }
    }
    return candidates;
  }

  private MethodHandle findInMethods() throws IllegalAccessException {
    List<Method> candidates = getCandidates();
    if (candidates.isEmpty()) { return null; }
    if (candidates.size() == 1) { return toMethodHandle(candidates.get(0)); }

    for (Method method : candidates) {
      if (isMethodDecorated(method)) {
        return toMethodHandle(method);
      }
      Class<?>[] parameterTypes = method.getParameterTypes();
      Object[] argsWithoutReceiver = copyOfRange(args, 1, args.length);
      if (TypeMatching.haveSameNumberOfArguments(argsWithoutReceiver, parameterTypes) || TypeMatching.haveEnoughArgumentsForVarargs(argsWithoutReceiver, method, parameterTypes)) {
        if (TypeMatching.canAssign(parameterTypes, argsWithoutReceiver, method.isVarArgs())) {
          return toMethodHandle(method);
        }
      }
    }
    return null;
  }

  private MethodHandle findInFields() throws IllegalAccessException {
    if (arity > 3) { return null; }

    for (Field field : receiverClass.getDeclaredFields()) {
      if (isMatchingField(field)) {
        return toMethodHandle(field);
      }
    }
    for (Field field : receiverClass.getFields()) {
      if (isMatchingField(field)) {
        return toMethodHandle(field);
      }
    }
    return null;
  }

  private boolean isMatchingField(Field field) {
    return field.getName().equals(methodName) && !isStatic(field.getModifiers());
  }

  private boolean isCandidateMethod(Method method) {
    return method.getName().equals(methodName) && isPublic(method.getModifiers()) && !isAbstract(method.getModifiers());
  }
}
