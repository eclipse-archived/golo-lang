/*
 * Copyright (c) 2012-2018 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.golo.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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

  private static final List<Class<?>> NUMBERS = java.util.Arrays.asList(
      Short.class,
      Character.class,
      Integer.class,
      Long.class,
      Float.class,
      Double.class);

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
    Class<?> boxedType = PRIMITIVE_MAP.get(type);
    Class<?> valueType = value.getClass();
    if (boxedType == valueType) {
      return true;
    }
    if (Number.class.isAssignableFrom(boxedType) && Number.class.isAssignableFrom(valueType)) {
      return NUMBERS.indexOf(boxedType) > NUMBERS.indexOf(valueType);
    }
    return false;
  }

  /**
   * Compare two types arrays for type compatibility, using lexicographic order.
   */
  public static int compareTypes(Class<?>[] types1, Class<?>[] types2) {
    if (types1.length != types2.length) {
      return Integer.compare(types1.length, types2.length);
    }
    for (int i = 0; i < types1.length; i++) {
      int cmp = compareSubstituable(types1[i], types2[i]);
      if (cmp != 0) {
        return cmp;
      }
    }
    return 0;
  }

  /**
   * Compare two type with a substituability relation (subtyping).
   *
   * <p>If the two type are not comparable, {@code 0} is returned so that the order is not changed in a stable sort.
   */
  public static int compareSubstituable(Class<?> type1, Class<?> type2) {
    Class<?> boxed1 = boxed(type1);
    Class<?> boxed2 = boxed(type2);
    if (boxed1 == boxed2) {
      return 0;
    }
    if (boxed1.isAssignableFrom(boxed2)) {
      return 1;
    }
    if (boxed2.isAssignableFrom(boxed1)) {
      return -1;
    }
    if (Number.class.isAssignableFrom(boxed1) && Number.class.isAssignableFrom(boxed2)) {
      return Integer.compare(NUMBERS.indexOf(boxed1), NUMBERS.indexOf(boxed2));
    }
    return 0;
  }

  private static Class<?> boxed(Class<?> t) {
    if (!t.isPrimitive()) {
      return t;
    }
    return PRIMITIVE_MAP.get(t);
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
