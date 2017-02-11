/*
 * Copyright (c) 2012-2016 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.golo.runtime.DecoratorsHelper.isMethodDecorated;
import static java.util.Arrays.copyOfRange;
import static gololang.Predefined.isClosure;

public final class TypeMatching {

  private TypeMatching() {
    throw new UnsupportedOperationException("Don't instantiate utility classes");
  }

  private static final Map<Class<?>, Class<?>> PRIMITIVE_MAP = new HashMap<Class<?>, Class<?>>() {
    {
      put(byte.class, Byte.class);
      put(short.class, Short.class);
      put(char.class, Character.class);
      put(int.class, Integer.class);
      put(long.class, Long.class);
      put(float.class, Float.class);
      put(double.class, Double.class);
      put(boolean.class, Boolean.class);
    }
  };

  public static boolean canAssign(Class<?>[] types, Object[] arguments, boolean varArgs) {
    if (types.length == 0 || arguments.length == 0) {
      return true;
    }
    for (int i = 0; i < types.length - 1; i++) {
      if (!valueAndTypeMatch(types[i], arguments[i])) {
        return false;
      }
    }
    final int last = types.length - 1;
    if (varArgs && arguments.length == last) {
      return true;
    }
    if (last >= arguments.length) {
      return false;
    }
    if (varArgs && !(arguments[last] instanceof Object[])) {
      return valueAndTypeMatch(types[last].getComponentType(), arguments[last]);
    }
    return valueAndTypeMatch(types[last], arguments[last]);
  }

  private static boolean valueAndTypeMatch(Class<?> type, Object value) {
    if (type == null) {
      return false;
    }
    return primitiveCompatible(type, value)
      || (type.isInstance(value)
          || value == null
          || samAssignment(type, value)
          || functionalInterfaceAssignment(type, value));
  }

  public static boolean functionalInterfaceAssignment(Class<?> type, Object value) {
    return isClosure(value) && isFunctionalInterface(type);
  }

  public static boolean samAssignment(Class<?> type, Object value) {
    return isClosure(value) && isSAM(type);
  }

  public static boolean isSAM(Class<?> type) {
    return type.isInterface() && (type.getMethods().length == 1);
  }

  public static boolean isFunctionalInterface(Class<?> type) {
    return type.isAnnotationPresent(FunctionalInterface.class);
  }

  private static boolean primitiveCompatible(Class<?> type, Object value) {
    if (!type.isPrimitive() || value == null) {
      return false;
    }
    return PRIMITIVE_MAP.get(type) == value.getClass();
  }

  public static boolean isLastArgumentAnArray(int index, Object[] args) {
    return index > 0 && args.length == index && args[index - 1] instanceof Object[];
  }

  public static boolean argumentsNumberMatches(int paramsNumber, int argsNumber, boolean isVarArgs) {
    return argsNumber < 0
           || (!isVarArgs && paramsNumber == argsNumber)
           || (isVarArgs && argsNumber >= paramsNumber - 1);
  }

  public static boolean argumentsMatch(Method method, Object[] arguments) {
    return argumentsMatch(method, arguments, method.isVarArgs());
  }

  public static boolean argumentsMatch(Method method, Object[] arguments, boolean varargs) {
    Object[] args = Modifier.isStatic(method.getModifiers())
      ? arguments
      : copyOfRange(arguments, 1, arguments.length);
    return
      isMethodDecorated(method)
      || (argumentsNumberMatches(method.getParameterCount(), args.length, varargs)
          && canAssign(method.getParameterTypes(), args, varargs));
  }

  public static boolean argumentsMatch(Constructor<?> constructor, Object[] arguments) {
    return
      argumentsNumberMatches(constructor.getParameterCount(), arguments.length, constructor.isVarArgs())
      && canAssign(constructor.getParameterTypes(), arguments, constructor.isVarArgs());
  }

  public static boolean returnsValue(Method m) {
    return !(m.getReturnType().equals(void.class) || m.getReturnType().equals(Void.class));
  }
}
