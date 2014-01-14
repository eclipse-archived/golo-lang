/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.*;
import java.util.HashSet;
import java.util.Set;

import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;

public class OperatorSupport {

  static class MonomorphicInlineCache extends MutableCallSite {

    final MethodHandles.Lookup callerLookup;
    final String name;
    MethodHandle fallback;

    MonomorphicInlineCache(MethodHandles.Lookup callerLookup, String name, MethodType type) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
    }
  }

  private static final MethodHandle GUARD_1;
  private static final MethodHandle FALLBACK_1;

  private static final MethodHandle GUARD_2;
  private static final MethodHandle FALLBACK_2;

  private static final Set<String> NO_GUARD_OPERATORS = new HashSet<String>() {
    {
      add("is");
      add("isnt");
      add("oftype");

      add("equals");
      add("notequals");

      add("more");
      add("less");
      add("moreorequals");
      add("lessorequals");

      add("orifnull");
    }
  };

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      GUARD_1 = lookup.findStatic(
          OperatorSupport.class,
          "guard_1",
          methodType(boolean.class, Class.class, Object.class));

      FALLBACK_1 = lookup.findStatic(
          OperatorSupport.class,
          "fallback_1",
          methodType(Object.class, MonomorphicInlineCache.class, Object[].class));

      GUARD_2 = lookup.findStatic(
          OperatorSupport.class,
          "guard_2",
          methodType(boolean.class, Class.class, Class.class, Object.class, Object.class));

      FALLBACK_2 = lookup.findStatic(
          OperatorSupport.class,
          "fallback_2",
          methodType(Object.class, MonomorphicInlineCache.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static boolean guard_1(Class<?> expected, Object arg) {
    Class<?> t = (arg == null) ? Object.class : arg.getClass();
    return (t == expected);
  }

  public static boolean guard_2(Class<?> expected1, Class<?> expected2, Object arg1, Object arg2) {
    Class<?> t1 = (arg1 == null) ? Object.class : arg1.getClass();
    Class<?> t2 = (arg2 == null) ? Object.class : arg2.getClass();
    return (t1 == expected1) && (t2 == expected2);
  }

  public static Object fallback_1(MonomorphicInlineCache inlineCache, Object[] args) throws Throwable {

    Class<?> argClass = (args[0] == null) ? Object.class : args[0].getClass();
    MethodHandle target;

    try {
      target = inlineCache.callerLookup.findStatic(
          OperatorSupport.class, inlineCache.name, methodType(Object.class, argClass));
    } catch (Throwable t1) {
      try {
        target = inlineCache.callerLookup.findStatic(
            OperatorSupport.class, inlineCache.name + "_fallback", methodType(Object.class, Object.class));
      } catch (Throwable t2) {
        return reject(args[0], inlineCache.name);
      }
    }
    target = target.asType(methodType(Object.class, Object.class));

    MethodHandle guard = GUARD_1.bindTo(argClass);

    MethodHandle guardedTarget = guardWithTest(guard, target, inlineCache.fallback);
    inlineCache.setTarget(guardedTarget);
    return target.invokeWithArguments(args);
  }

  public static Object fallback_2(MonomorphicInlineCache inlineCache, Object[] args) throws Throwable {

    Class<?> arg1Class = (args[0] == null) ? Object.class : args[0].getClass();
    Class<?> arg2Class = (args[1] == null) ? Object.class : args[1].getClass();
    MethodHandle target;

    try {
      target = inlineCache.callerLookup.findStatic(
          OperatorSupport.class, inlineCache.name, methodType(Object.class, arg1Class, arg2Class));
    } catch (Throwable t1) {
      try {
        target = inlineCache.callerLookup.findStatic(
            OperatorSupport.class, inlineCache.name + "_fallback", methodType(Object.class, Object.class, Object.class));
      } catch (Throwable t2) {
        return reject(args[0], args[1], inlineCache.name);
      }
    }
    target = target.asType(methodType(Object.class, Object.class, Object.class));

    MethodHandle guard = insertArguments(GUARD_2, 0, arg1Class, arg2Class);

    MethodHandle guardedTarget = guardWithTest(guard, target, inlineCache.fallback);
    inlineCache.setTarget(guardedTarget);
    return target.invokeWithArguments(args);
  }

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type, int arity) throws NoSuchMethodException, IllegalAccessException {

    if (NO_GUARD_OPERATORS.contains(name)) {
      MethodHandle target = caller.findStatic(OperatorSupport.class, name + "_noguard",
          methodType(Object.class, Object.class, Object.class));
      return new ConstantCallSite(target);
    }

    MonomorphicInlineCache callSite = new MonomorphicInlineCache(caller, name, type);
    MethodHandle fallback;
    if (arity == 2) {
      fallback = FALLBACK_2;
    } else {
      fallback = FALLBACK_1;
    }
    MethodHandle fallbackHandle = fallback
        .bindTo(callSite)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    callSite.fallback = fallbackHandle;
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  // arithmetic (generated, use generate_math.rb) ......................................................................

  public static Object plus(Character a, Character b) {
    return a + b;
  }

  public static Object minus(Character a, Character b) {
    return a - b;
  }

  public static Object divide(Character a, Character b) {
    return a / b;
  }

  public static Object times(Character a, Character b) {
    return a * b;
  }

  public static Object modulo(Character a, Character b) {
    return a % b;
  }

  public static Object plus(Integer a, Integer b) {
    return a + b;
  }

  public static Object minus(Integer a, Integer b) {
    return a - b;
  }

  public static Object divide(Integer a, Integer b) {
    return a / b;
  }

  public static Object times(Integer a, Integer b) {
    return a * b;
  }

  public static Object modulo(Integer a, Integer b) {
    return a % b;
  }

  public static Object plus(Long a, Long b) {
    return a + b;
  }

  public static Object minus(Long a, Long b) {
    return a - b;
  }

  public static Object divide(Long a, Long b) {
    return a / b;
  }

  public static Object times(Long a, Long b) {
    return a * b;
  }

  public static Object modulo(Long a, Long b) {
    return a % b;
  }

  public static Object plus(Double a, Double b) {
    return a + b;
  }

  public static Object minus(Double a, Double b) {
    return a - b;
  }

  public static Object divide(Double a, Double b) {
    return a / b;
  }

  public static Object times(Double a, Double b) {
    return a * b;
  }

  public static Object modulo(Double a, Double b) {
    return a % b;
  }

  public static Object plus(Float a, Float b) {
    return a + b;
  }

  public static Object minus(Float a, Float b) {
    return a - b;
  }

  public static Object divide(Float a, Float b) {
    return a / b;
  }

  public static Object times(Float a, Float b) {
    return a * b;
  }

  public static Object modulo(Float a, Float b) {
    return a % b;
  }

  public static Object plus(Character a, Integer b) {
    return ((int) a) + b;
  }

  public static Object minus(Character a, Integer b) {
    return ((int) a) - b;
  }

  public static Object divide(Character a, Integer b) {
    return ((int) a) / b;
  }

  public static Object times(Character a, Integer b) {
    return ((int) a) * b;
  }

  public static Object modulo(Character a, Integer b) {
    return ((int) a) % b;
  }

  public static Object plus(Character a, Long b) {
    return ((long) a) + b;
  }

  public static Object minus(Character a, Long b) {
    return ((long) a) - b;
  }

  public static Object divide(Character a, Long b) {
    return ((long) a) / b;
  }

  public static Object times(Character a, Long b) {
    return ((long) a) * b;
  }

  public static Object modulo(Character a, Long b) {
    return ((long) a) % b;
  }

  public static Object plus(Character a, Double b) {
    return ((double) a) + b;
  }

  public static Object minus(Character a, Double b) {
    return ((double) a) - b;
  }

  public static Object divide(Character a, Double b) {
    return ((double) a) / b;
  }

  public static Object times(Character a, Double b) {
    return ((double) a) * b;
  }

  public static Object modulo(Character a, Double b) {
    return ((double) a) % b;
  }

  public static Object plus(Character a, Float b) {
    return ((float) a) + b;
  }

  public static Object minus(Character a, Float b) {
    return ((float) a) - b;
  }

  public static Object divide(Character a, Float b) {
    return ((float) a) / b;
  }

  public static Object times(Character a, Float b) {
    return ((float) a) * b;
  }

  public static Object modulo(Character a, Float b) {
    return ((float) a) % b;
  }

  public static Object plus(Integer a, Long b) {
    return ((long) a) + b;
  }

  public static Object minus(Integer a, Long b) {
    return ((long) a) - b;
  }

  public static Object divide(Integer a, Long b) {
    return ((long) a) / b;
  }

  public static Object times(Integer a, Long b) {
    return ((long) a) * b;
  }

  public static Object modulo(Integer a, Long b) {
    return ((long) a) % b;
  }

  public static Object plus(Integer a, Double b) {
    return ((double) a) + b;
  }

  public static Object minus(Integer a, Double b) {
    return ((double) a) - b;
  }

  public static Object divide(Integer a, Double b) {
    return ((double) a) / b;
  }

  public static Object times(Integer a, Double b) {
    return ((double) a) * b;
  }

  public static Object modulo(Integer a, Double b) {
    return ((double) a) % b;
  }

  public static Object plus(Integer a, Float b) {
    return ((float) a) + b;
  }

  public static Object minus(Integer a, Float b) {
    return ((float) a) - b;
  }

  public static Object divide(Integer a, Float b) {
    return ((float) a) / b;
  }

  public static Object times(Integer a, Float b) {
    return ((float) a) * b;
  }

  public static Object modulo(Integer a, Float b) {
    return ((float) a) % b;
  }

  public static Object plus(Long a, Double b) {
    return ((double) a) + b;
  }

  public static Object minus(Long a, Double b) {
    return ((double) a) - b;
  }

  public static Object divide(Long a, Double b) {
    return ((double) a) / b;
  }

  public static Object times(Long a, Double b) {
    return ((double) a) * b;
  }

  public static Object modulo(Long a, Double b) {
    return ((double) a) % b;
  }

  public static Object plus(Long a, Float b) {
    return ((float) a) + b;
  }

  public static Object minus(Long a, Float b) {
    return ((float) a) - b;
  }

  public static Object divide(Long a, Float b) {
    return ((float) a) / b;
  }

  public static Object times(Long a, Float b) {
    return ((float) a) * b;
  }

  public static Object modulo(Long a, Float b) {
    return ((float) a) % b;
  }

  public static Object plus(Double a, Float b) {
    return a + ((double) b);
  }

  public static Object minus(Double a, Float b) {
    return a - ((double) b);
  }

  public static Object divide(Double a, Float b) {
    return a / ((double) b);
  }

  public static Object times(Double a, Float b) {
    return a * ((double) b);
  }

  public static Object modulo(Double a, Float b) {
    return a % ((double) b);
  }

  public static Object plus(Integer a, Character b) {
    return a + ((int) b);
  }

  public static Object minus(Integer a, Character b) {
    return a - ((int) b);
  }

  public static Object divide(Integer a, Character b) {
    return a / ((int) b);
  }

  public static Object times(Integer a, Character b) {
    return a * ((int) b);
  }

  public static Object modulo(Integer a, Character b) {
    return a % ((int) b);
  }

  public static Object plus(Long a, Character b) {
    return a + ((long) b);
  }

  public static Object minus(Long a, Character b) {
    return a - ((long) b);
  }

  public static Object divide(Long a, Character b) {
    return a / ((long) b);
  }

  public static Object times(Long a, Character b) {
    return a * ((long) b);
  }

  public static Object modulo(Long a, Character b) {
    return a % ((long) b);
  }

  public static Object plus(Double a, Character b) {
    return a + ((double) b);
  }

  public static Object minus(Double a, Character b) {
    return a - ((double) b);
  }

  public static Object divide(Double a, Character b) {
    return a / ((double) b);
  }

  public static Object times(Double a, Character b) {
    return a * ((double) b);
  }

  public static Object modulo(Double a, Character b) {
    return a % ((double) b);
  }

  public static Object plus(Float a, Character b) {
    return a + ((float) b);
  }

  public static Object minus(Float a, Character b) {
    return a - ((float) b);
  }

  public static Object divide(Float a, Character b) {
    return a / ((float) b);
  }

  public static Object times(Float a, Character b) {
    return a * ((float) b);
  }

  public static Object modulo(Float a, Character b) {
    return a % ((float) b);
  }

  public static Object plus(Long a, Integer b) {
    return a + ((long) b);
  }

  public static Object minus(Long a, Integer b) {
    return a - ((long) b);
  }

  public static Object divide(Long a, Integer b) {
    return a / ((long) b);
  }

  public static Object times(Long a, Integer b) {
    return a * ((long) b);
  }

  public static Object modulo(Long a, Integer b) {
    return a % ((long) b);
  }

  public static Object plus(Double a, Integer b) {
    return a + ((double) b);
  }

  public static Object minus(Double a, Integer b) {
    return a - ((double) b);
  }

  public static Object divide(Double a, Integer b) {
    return a / ((double) b);
  }

  public static Object times(Double a, Integer b) {
    return a * ((double) b);
  }

  public static Object modulo(Double a, Integer b) {
    return a % ((double) b);
  }

  public static Object plus(Float a, Integer b) {
    return a + ((float) b);
  }

  public static Object minus(Float a, Integer b) {
    return a - ((float) b);
  }

  public static Object divide(Float a, Integer b) {
    return a / ((float) b);
  }

  public static Object times(Float a, Integer b) {
    return a * ((float) b);
  }

  public static Object modulo(Float a, Integer b) {
    return a % ((float) b);
  }

  public static Object plus(Double a, Long b) {
    return a + ((double) b);
  }

  public static Object minus(Double a, Long b) {
    return a - ((double) b);
  }

  public static Object divide(Double a, Long b) {
    return a / ((double) b);
  }

  public static Object times(Double a, Long b) {
    return a * ((double) b);
  }

  public static Object modulo(Double a, Long b) {
    return a % ((double) b);
  }

  public static Object plus(Float a, Long b) {
    return a + ((float) b);
  }

  public static Object minus(Float a, Long b) {
    return a - ((float) b);
  }

  public static Object divide(Float a, Long b) {
    return a / ((float) b);
  }

  public static Object times(Float a, Long b) {
    return a * ((float) b);
  }

  public static Object modulo(Float a, Long b) {
    return a % ((float) b);
  }

  public static Object plus(Float a, Double b) {
    return ((double) a) + b;
  }

  public static Object minus(Float a, Double b) {
    return ((double) a) - b;
  }

  public static Object divide(Float a, Double b) {
    return ((double) a) / b;
  }

  public static Object times(Float a, Double b) {
    return ((double) a) * b;
  }

  public static Object modulo(Float a, Double b) {
    return ((double) a) % b;
  }

  // arithmetic fallbacks .............................................................................................

  public static Object plus(String a, String b) {
    return a + b;
  }

  public static Object plus_fallback(Object a, Object b) {
    if (isNotNullAndString(a) || isNotNullAndString(b)) {
      return String.valueOf(a) + b;
    }
    return reject(a, b, "plus");
  }

  public static Object times_fallback(Object a, Object b) {
    if (isInteger(a) && isString(b)) {
      return repeat((String) b, (Integer) a);
    }
    if (isString(a) && isInteger(b)) {
      return repeat((String) a, (Integer) b);
    }
    return reject(a, b, "times");
  }

  private static String repeat(String string, int n) {
    StringBuilder builder = new StringBuilder(string);
    for (int i = 1; i < n; i++) {
      builder.append(string);
    }
    return builder.toString();
  }

  // comparisons ......................................................................................................

  public static Object equals_noguard(Object a, Object b) {
    return (a == b) || ((a != null) && a.equals(b));
  }

  public static Object notequals_noguard(Object a, Object b) {
    return (a != b) && (((a != null) && !a.equals(b)) || ((b != null) && !b.equals(a)));
  }

  @SuppressWarnings("unchecked")
  public static Object less_noguard(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) < 0;
    }
    return reject(a, b, "less");
  }

  @SuppressWarnings("unchecked")
  public static Object lessorequals_noguard(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) <= 0;
    }
    return reject(a, b, "lessorequals");
  }

  @SuppressWarnings("unchecked")
  public static Object more_noguard(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) > 0;
    }
    return reject(a, b, "more");
  }

  @SuppressWarnings("unchecked")
  public static Object moreorequals_noguard(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) >= 0;
    }
    return reject(a, b, "moreorequals");
  }

  // logic ............................................................................................................

  public static Object not(Boolean a) {
    return !a;
  }

  public static Object oftype_noguard(Object a, Object b) {
    if (isClass(b)) {
      return ((Class<?>) b).isInstance(a);
    }
    return reject(a, b, "oftype");
  }

  public static Object is_noguard(Object a, Object b) {
    return a == b;
  }

  public static Object isnt_noguard(Object a, Object b) {
    return a != b;
  }

  public static Object orifnull_noguard(Object a, Object b) {
    return (a != null) ? a : b;
  }

  // helpers ..........................................................................................................

  private static boolean isNotNullAndString(Object obj) {
    return (obj != null) && (obj.getClass() == String.class);
  }

  private static boolean bothNotNull(Object a, Object b) {
    return (a != null) && (b != null);
  }

  private static boolean isString(Object obj) {
    return obj.getClass() == String.class;
  }

  private static boolean isInteger(Object obj) {
    return obj.getClass() == Integer.class;
  }

  private static boolean isComparable(Object obj) {
    return obj instanceof Comparable<?>;
  }

  private static boolean isClass(Object obj) {
    return (obj != null) && (obj.getClass() == Class.class);
  }

  private static Object reject(Object a, String symbol) throws IllegalArgumentException {
    throw new IllegalArgumentException(String.format("Operator %s is not supported for type %s", symbol, a.getClass()));
  }

  private static Object reject(Object a, Object b, String symbol) throws IllegalArgumentException {
    throw new IllegalArgumentException(String.format("Operator %s is not supported for types %s and %s", symbol, a.getClass(), b.getClass()));
  }
}
