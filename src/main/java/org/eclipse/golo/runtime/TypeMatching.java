/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import gololang.FunctionReference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TypeMatching {

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

  public static boolean haveEnoughArgumentsForVarargs(Object[] arguments, Constructor<?> constructor, Class<?>[] parameterTypes) {
    return constructor.isVarArgs() && (arguments.length >= parameterTypes.length);
  }

  public static boolean haveEnoughArgumentsForVarargs(Object[] arguments, Method method, Class<?>[] parameterTypes) {
    return method.isVarArgs() && (arguments.length >= (parameterTypes.length - 1));
  }

  public static boolean haveSameNumberOfArguments(Object[] arguments, Class<?>[] parameterTypes) {
    return parameterTypes.length == arguments.length;
  }

  public static boolean argumentsNumberMatch(Object[] arguments, Method method, Class<?>[] parameterTypes) {
    return haveSameNumberOfArguments(arguments, parameterTypes)
      || haveEnoughArgumentsForVarargs(arguments, method, parameterTypes);
  }

  public static boolean argumentsNumberMatch(Object[] arguments, Constructor<?> constructor, Class<?>[] parameterTypes) {
    return haveSameNumberOfArguments(arguments, parameterTypes)
      || haveEnoughArgumentsForVarargs(arguments, constructor, parameterTypes);
  }

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

  public static boolean valueAndTypeMatch(Class<?> type, Object value) {
    return primitiveCompatible(type, value) || (type.isInstance(value) || value == null || samAssignment(type, value) || functionalInterfaceAssignment(type, value));
  }

  private static boolean functionalInterfaceAssignment(Class<?> type, Object value) {
    return (value instanceof FunctionReference) && isFunctionalInterface(type);
  }

  public static boolean samAssignment(Class<?> type, Object value) {
    return (value instanceof FunctionReference) && isSAM(type);
  }

  public static boolean isSAM(Class<?> type) {
    return type.isInterface() && (type.getMethods().length == 1);
  }

  public static boolean isFunctionalInterface(Class<?> type) {
    return type.isAnnotationPresent(FunctionalInterface.class);
  }

  public static boolean primitiveCompatible(Class<?> type, Object value) {
    if (!type.isPrimitive() || value == null) {
      return false;
    }
    return PRIMITIVE_MAP.get(type) == value.getClass();
  }

  public static boolean isLastArgumentAnArray(int index, Object[] args) {
    return index > 0 && args.length == index && args[index - 1] instanceof Object[];
  }
}
