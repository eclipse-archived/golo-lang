/*
 * Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.golo.runtime;

import java.lang.invoke.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.math.BigDecimal;
import java.math.BigInteger;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;
import static gololang.Messages.message;

public final class OperatorSupport {

  private OperatorSupport() {
    throw new UnsupportedOperationException("Don't instantiate invokedynamic bootstrap class");
  }

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

  private static final MethodHandle FALLBACK_1;

  private static final MethodHandle GUARD_2;
  private static final MethodHandle FALLBACK_2;

  private static final Set<String> NO_GUARD_OPERATORS = new HashSet<String>() {
    {
      add("is");
      add("isnt");
      add("oftype");
    }
  };

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

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
    target = catchException(target, ClassCastException.class, inlineCache.fallback);
    inlineCache.setTarget(target);

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
    if (arg1Class == String.class || arg2Class == String.class) {
      MethodHandle guard = insertArguments(GUARD_2, 0, arg1Class, arg2Class);
      target = guardWithTest(guard, target, inlineCache.fallback);
    } else {
      target = catchException(target, ClassCastException.class, dropArguments(inlineCache.fallback, 0, ClassCastException.class));
    }
    inlineCache.setTarget(target);

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

  // primitive specific arithmetic and comparisons (generated, use generate_math.rb) ......................................................................

  // BEGIN GENERATED
  public static Object plus(Character a, Character b) {
    return ((char) a) + ((char) b);
  }

  public static Object minus(Character a, Character b) {
    return ((char) a) - ((char) b);
  }

  public static Object divide(Character a, Character b) {
    return ((char) a) / ((char) b);
  }

  public static Object times(Character a, Character b) {
    return ((char) a) * ((char) b);
  }

  public static Object modulo(Character a, Character b) {
    return ((char) a) % ((char) b);
  }

  public static Object equals(Character a, Character b) {
    return ((char) a) == ((char) b);
  }

  public static Object notequals(Character a, Character b) {
    return ((char) a) != ((char) b);
  }

  public static Object less(Character a, Character b) {
    return ((char) a) < ((char) b);
  }

  public static Object lessorequals(Character a, Character b) {
    return ((char) a) <= ((char) b);
  }

  public static Object more(Character a, Character b) {
    return ((char) a) > ((char) b);
  }

  public static Object moreorequals(Character a, Character b) {
    return ((char) a) >= ((char) b);
  }

  public static Object plus(Integer a, Integer b) {
    return ((int) a) + ((int) b);
  }

  public static Object minus(Integer a, Integer b) {
    return ((int) a) - ((int) b);
  }

  public static Object divide(Integer a, Integer b) {
    return ((int) a) / ((int) b);
  }

  public static Object times(Integer a, Integer b) {
    return ((int) a) * ((int) b);
  }

  public static Object modulo(Integer a, Integer b) {
    return ((int) a) % ((int) b);
  }

  public static Object equals(Integer a, Integer b) {
    return ((int) a) == ((int) b);
  }

  public static Object notequals(Integer a, Integer b) {
    return ((int) a) != ((int) b);
  }

  public static Object less(Integer a, Integer b) {
    return ((int) a) < ((int) b);
  }

  public static Object lessorequals(Integer a, Integer b) {
    return ((int) a) <= ((int) b);
  }

  public static Object more(Integer a, Integer b) {
    return ((int) a) > ((int) b);
  }

  public static Object moreorequals(Integer a, Integer b) {
    return ((int) a) >= ((int) b);
  }

  public static Object plus(Long a, Long b) {
    return ((long) a) + ((long) b);
  }

  public static Object minus(Long a, Long b) {
    return ((long) a) - ((long) b);
  }

  public static Object divide(Long a, Long b) {
    return ((long) a) / ((long) b);
  }

  public static Object times(Long a, Long b) {
    return ((long) a) * ((long) b);
  }

  public static Object modulo(Long a, Long b) {
    return ((long) a) % ((long) b);
  }

  public static Object equals(Long a, Long b) {
    return ((long) a) == ((long) b);
  }

  public static Object notequals(Long a, Long b) {
    return ((long) a) != ((long) b);
  }

  public static Object less(Long a, Long b) {
    return ((long) a) < ((long) b);
  }

  public static Object lessorequals(Long a, Long b) {
    return ((long) a) <= ((long) b);
  }

  public static Object more(Long a, Long b) {
    return ((long) a) > ((long) b);
  }

  public static Object moreorequals(Long a, Long b) {
    return ((long) a) >= ((long) b);
  }

  public static Object plus(Double a, Double b) {
    return ((double) a) + ((double) b);
  }

  public static Object minus(Double a, Double b) {
    return ((double) a) - ((double) b);
  }

  public static Object divide(Double a, Double b) {
    return ((double) a) / ((double) b);
  }

  public static Object times(Double a, Double b) {
    return ((double) a) * ((double) b);
  }

  public static Object modulo(Double a, Double b) {
    return ((double) a) % ((double) b);
  }

  public static Object equals(Double a, Double b) {
    return ((double) a) == ((double) b);
  }

  public static Object notequals(Double a, Double b) {
    return ((double) a) != ((double) b);
  }

  public static Object less(Double a, Double b) {
    return ((double) a) < ((double) b);
  }

  public static Object lessorequals(Double a, Double b) {
    return ((double) a) <= ((double) b);
  }

  public static Object more(Double a, Double b) {
    return ((double) a) > ((double) b);
  }

  public static Object moreorequals(Double a, Double b) {
    return ((double) a) >= ((double) b);
  }

  public static Object plus(Float a, Float b) {
    return ((float) a) + ((float) b);
  }

  public static Object minus(Float a, Float b) {
    return ((float) a) - ((float) b);
  }

  public static Object divide(Float a, Float b) {
    return ((float) a) / ((float) b);
  }

  public static Object times(Float a, Float b) {
    return ((float) a) * ((float) b);
  }

  public static Object modulo(Float a, Float b) {
    return ((float) a) % ((float) b);
  }

  public static Object equals(Float a, Float b) {
    return ((float) a) == ((float) b);
  }

  public static Object notequals(Float a, Float b) {
    return ((float) a) != ((float) b);
  }

  public static Object less(Float a, Float b) {
    return ((float) a) < ((float) b);
  }

  public static Object lessorequals(Float a, Float b) {
    return ((float) a) <= ((float) b);
  }

  public static Object more(Float a, Float b) {
    return ((float) a) > ((float) b);
  }

  public static Object moreorequals(Float a, Float b) {
    return ((float) a) >= ((float) b);
  }

  public static Object plus(Character a, Integer b) {
    return ((int) a) + ((int) b);
  }

  public static Object minus(Character a, Integer b) {
    return ((int) a) - ((int) b);
  }

  public static Object divide(Character a, Integer b) {
    return ((int) a) / ((int) b);
  }

  public static Object times(Character a, Integer b) {
    return ((int) a) * ((int) b);
  }

  public static Object modulo(Character a, Integer b) {
    return ((int) a) % ((int) b);
  }

  public static Object equals(Character a, Integer b) {
    return ((int) a) == ((int) b);
  }

  public static Object notequals(Character a, Integer b) {
    return ((int) a) != ((int) b);
  }

  public static Object less(Character a, Integer b) {
    return ((int) a) < ((int) b);
  }

  public static Object lessorequals(Character a, Integer b) {
    return ((int) a) <= ((int) b);
  }

  public static Object more(Character a, Integer b) {
    return ((int) a) > ((int) b);
  }

  public static Object moreorequals(Character a, Integer b) {
    return ((int) a) >= ((int) b);
  }

  public static Object plus(Character a, Long b) {
    return ((long) a) + ((long) b);
  }

  public static Object minus(Character a, Long b) {
    return ((long) a) - ((long) b);
  }

  public static Object divide(Character a, Long b) {
    return ((long) a) / ((long) b);
  }

  public static Object times(Character a, Long b) {
    return ((long) a) * ((long) b);
  }

  public static Object modulo(Character a, Long b) {
    return ((long) a) % ((long) b);
  }

  public static Object equals(Character a, Long b) {
    return ((long) a) == ((long) b);
  }

  public static Object notequals(Character a, Long b) {
    return ((long) a) != ((long) b);
  }

  public static Object less(Character a, Long b) {
    return ((long) a) < ((long) b);
  }

  public static Object lessorequals(Character a, Long b) {
    return ((long) a) <= ((long) b);
  }

  public static Object more(Character a, Long b) {
    return ((long) a) > ((long) b);
  }

  public static Object moreorequals(Character a, Long b) {
    return ((long) a) >= ((long) b);
  }

  public static Object plus(Character a, Double b) {
    return ((double) a) + ((double) b);
  }

  public static Object minus(Character a, Double b) {
    return ((double) a) - ((double) b);
  }

  public static Object divide(Character a, Double b) {
    return ((double) a) / ((double) b);
  }

  public static Object times(Character a, Double b) {
    return ((double) a) * ((double) b);
  }

  public static Object modulo(Character a, Double b) {
    return ((double) a) % ((double) b);
  }

  public static Object equals(Character a, Double b) {
    return ((double) a) == ((double) b);
  }

  public static Object notequals(Character a, Double b) {
    return ((double) a) != ((double) b);
  }

  public static Object less(Character a, Double b) {
    return ((double) a) < ((double) b);
  }

  public static Object lessorequals(Character a, Double b) {
    return ((double) a) <= ((double) b);
  }

  public static Object more(Character a, Double b) {
    return ((double) a) > ((double) b);
  }

  public static Object moreorequals(Character a, Double b) {
    return ((double) a) >= ((double) b);
  }

  public static Object plus(Character a, Float b) {
    return ((float) a) + ((float) b);
  }

  public static Object minus(Character a, Float b) {
    return ((float) a) - ((float) b);
  }

  public static Object divide(Character a, Float b) {
    return ((float) a) / ((float) b);
  }

  public static Object times(Character a, Float b) {
    return ((float) a) * ((float) b);
  }

  public static Object modulo(Character a, Float b) {
    return ((float) a) % ((float) b);
  }

  public static Object equals(Character a, Float b) {
    return ((float) a) == ((float) b);
  }

  public static Object notequals(Character a, Float b) {
    return ((float) a) != ((float) b);
  }

  public static Object less(Character a, Float b) {
    return ((float) a) < ((float) b);
  }

  public static Object lessorequals(Character a, Float b) {
    return ((float) a) <= ((float) b);
  }

  public static Object more(Character a, Float b) {
    return ((float) a) > ((float) b);
  }

  public static Object moreorequals(Character a, Float b) {
    return ((float) a) >= ((float) b);
  }

  public static Object plus(Integer a, Long b) {
    return ((long) a) + ((long) b);
  }

  public static Object minus(Integer a, Long b) {
    return ((long) a) - ((long) b);
  }

  public static Object divide(Integer a, Long b) {
    return ((long) a) / ((long) b);
  }

  public static Object times(Integer a, Long b) {
    return ((long) a) * ((long) b);
  }

  public static Object modulo(Integer a, Long b) {
    return ((long) a) % ((long) b);
  }

  public static Object equals(Integer a, Long b) {
    return ((long) a) == ((long) b);
  }

  public static Object notequals(Integer a, Long b) {
    return ((long) a) != ((long) b);
  }

  public static Object less(Integer a, Long b) {
    return ((long) a) < ((long) b);
  }

  public static Object lessorequals(Integer a, Long b) {
    return ((long) a) <= ((long) b);
  }

  public static Object more(Integer a, Long b) {
    return ((long) a) > ((long) b);
  }

  public static Object moreorequals(Integer a, Long b) {
    return ((long) a) >= ((long) b);
  }

  public static Object plus(Integer a, Double b) {
    return ((double) a) + ((double) b);
  }

  public static Object minus(Integer a, Double b) {
    return ((double) a) - ((double) b);
  }

  public static Object divide(Integer a, Double b) {
    return ((double) a) / ((double) b);
  }

  public static Object times(Integer a, Double b) {
    return ((double) a) * ((double) b);
  }

  public static Object modulo(Integer a, Double b) {
    return ((double) a) % ((double) b);
  }

  public static Object equals(Integer a, Double b) {
    return ((double) a) == ((double) b);
  }

  public static Object notequals(Integer a, Double b) {
    return ((double) a) != ((double) b);
  }

  public static Object less(Integer a, Double b) {
    return ((double) a) < ((double) b);
  }

  public static Object lessorequals(Integer a, Double b) {
    return ((double) a) <= ((double) b);
  }

  public static Object more(Integer a, Double b) {
    return ((double) a) > ((double) b);
  }

  public static Object moreorequals(Integer a, Double b) {
    return ((double) a) >= ((double) b);
  }

  public static Object plus(Integer a, Float b) {
    return ((float) a) + ((float) b);
  }

  public static Object minus(Integer a, Float b) {
    return ((float) a) - ((float) b);
  }

  public static Object divide(Integer a, Float b) {
    return ((float) a) / ((float) b);
  }

  public static Object times(Integer a, Float b) {
    return ((float) a) * ((float) b);
  }

  public static Object modulo(Integer a, Float b) {
    return ((float) a) % ((float) b);
  }

  public static Object equals(Integer a, Float b) {
    return ((float) a) == ((float) b);
  }

  public static Object notequals(Integer a, Float b) {
    return ((float) a) != ((float) b);
  }

  public static Object less(Integer a, Float b) {
    return ((float) a) < ((float) b);
  }

  public static Object lessorequals(Integer a, Float b) {
    return ((float) a) <= ((float) b);
  }

  public static Object more(Integer a, Float b) {
    return ((float) a) > ((float) b);
  }

  public static Object moreorequals(Integer a, Float b) {
    return ((float) a) >= ((float) b);
  }

  public static Object plus(Long a, Double b) {
    return ((double) a) + ((double) b);
  }

  public static Object minus(Long a, Double b) {
    return ((double) a) - ((double) b);
  }

  public static Object divide(Long a, Double b) {
    return ((double) a) / ((double) b);
  }

  public static Object times(Long a, Double b) {
    return ((double) a) * ((double) b);
  }

  public static Object modulo(Long a, Double b) {
    return ((double) a) % ((double) b);
  }

  public static Object equals(Long a, Double b) {
    return ((double) a) == ((double) b);
  }

  public static Object notequals(Long a, Double b) {
    return ((double) a) != ((double) b);
  }

  public static Object less(Long a, Double b) {
    return ((double) a) < ((double) b);
  }

  public static Object lessorequals(Long a, Double b) {
    return ((double) a) <= ((double) b);
  }

  public static Object more(Long a, Double b) {
    return ((double) a) > ((double) b);
  }

  public static Object moreorequals(Long a, Double b) {
    return ((double) a) >= ((double) b);
  }

  public static Object plus(Long a, Float b) {
    return ((float) a) + ((float) b);
  }

  public static Object minus(Long a, Float b) {
    return ((float) a) - ((float) b);
  }

  public static Object divide(Long a, Float b) {
    return ((float) a) / ((float) b);
  }

  public static Object times(Long a, Float b) {
    return ((float) a) * ((float) b);
  }

  public static Object modulo(Long a, Float b) {
    return ((float) a) % ((float) b);
  }

  public static Object equals(Long a, Float b) {
    return ((float) a) == ((float) b);
  }

  public static Object notequals(Long a, Float b) {
    return ((float) a) != ((float) b);
  }

  public static Object less(Long a, Float b) {
    return ((float) a) < ((float) b);
  }

  public static Object lessorequals(Long a, Float b) {
    return ((float) a) <= ((float) b);
  }

  public static Object more(Long a, Float b) {
    return ((float) a) > ((float) b);
  }

  public static Object moreorequals(Long a, Float b) {
    return ((float) a) >= ((float) b);
  }

  public static Object plus(Double a, Float b) {
    return ((double) a) + ((double) b);
  }

  public static Object minus(Double a, Float b) {
    return ((double) a) - ((double) b);
  }

  public static Object divide(Double a, Float b) {
    return ((double) a) / ((double) b);
  }

  public static Object times(Double a, Float b) {
    return ((double) a) * ((double) b);
  }

  public static Object modulo(Double a, Float b) {
    return ((double) a) % ((double) b);
  }

  public static Object equals(Double a, Float b) {
    return ((double) a) == ((double) b);
  }

  public static Object notequals(Double a, Float b) {
    return ((double) a) != ((double) b);
  }

  public static Object less(Double a, Float b) {
    return ((double) a) < ((double) b);
  }

  public static Object lessorequals(Double a, Float b) {
    return ((double) a) <= ((double) b);
  }

  public static Object more(Double a, Float b) {
    return ((double) a) > ((double) b);
  }

  public static Object moreorequals(Double a, Float b) {
    return ((double) a) >= ((double) b);
  }

  public static Object plus(Integer a, Character b) {
    return ((int) a) + ((int) b);
  }

  public static Object minus(Integer a, Character b) {
    return ((int) a) - ((int) b);
  }

  public static Object divide(Integer a, Character b) {
    return ((int) a) / ((int) b);
  }

  public static Object times(Integer a, Character b) {
    return ((int) a) * ((int) b);
  }

  public static Object modulo(Integer a, Character b) {
    return ((int) a) % ((int) b);
  }

  public static Object equals(Integer a, Character b) {
    return ((int) a) == ((int) b);
  }

  public static Object notequals(Integer a, Character b) {
    return ((int) a) != ((int) b);
  }

  public static Object less(Integer a, Character b) {
    return ((int) a) < ((int) b);
  }

  public static Object lessorequals(Integer a, Character b) {
    return ((int) a) <= ((int) b);
  }

  public static Object more(Integer a, Character b) {
    return ((int) a) > ((int) b);
  }

  public static Object moreorequals(Integer a, Character b) {
    return ((int) a) >= ((int) b);
  }

  public static Object plus(Long a, Character b) {
    return ((long) a) + ((long) b);
  }

  public static Object minus(Long a, Character b) {
    return ((long) a) - ((long) b);
  }

  public static Object divide(Long a, Character b) {
    return ((long) a) / ((long) b);
  }

  public static Object times(Long a, Character b) {
    return ((long) a) * ((long) b);
  }

  public static Object modulo(Long a, Character b) {
    return ((long) a) % ((long) b);
  }

  public static Object equals(Long a, Character b) {
    return ((long) a) == ((long) b);
  }

  public static Object notequals(Long a, Character b) {
    return ((long) a) != ((long) b);
  }

  public static Object less(Long a, Character b) {
    return ((long) a) < ((long) b);
  }

  public static Object lessorequals(Long a, Character b) {
    return ((long) a) <= ((long) b);
  }

  public static Object more(Long a, Character b) {
    return ((long) a) > ((long) b);
  }

  public static Object moreorequals(Long a, Character b) {
    return ((long) a) >= ((long) b);
  }

  public static Object plus(Double a, Character b) {
    return ((double) a) + ((double) b);
  }

  public static Object minus(Double a, Character b) {
    return ((double) a) - ((double) b);
  }

  public static Object divide(Double a, Character b) {
    return ((double) a) / ((double) b);
  }

  public static Object times(Double a, Character b) {
    return ((double) a) * ((double) b);
  }

  public static Object modulo(Double a, Character b) {
    return ((double) a) % ((double) b);
  }

  public static Object equals(Double a, Character b) {
    return ((double) a) == ((double) b);
  }

  public static Object notequals(Double a, Character b) {
    return ((double) a) != ((double) b);
  }

  public static Object less(Double a, Character b) {
    return ((double) a) < ((double) b);
  }

  public static Object lessorequals(Double a, Character b) {
    return ((double) a) <= ((double) b);
  }

  public static Object more(Double a, Character b) {
    return ((double) a) > ((double) b);
  }

  public static Object moreorequals(Double a, Character b) {
    return ((double) a) >= ((double) b);
  }

  public static Object plus(Float a, Character b) {
    return ((float) a) + ((float) b);
  }

  public static Object minus(Float a, Character b) {
    return ((float) a) - ((float) b);
  }

  public static Object divide(Float a, Character b) {
    return ((float) a) / ((float) b);
  }

  public static Object times(Float a, Character b) {
    return ((float) a) * ((float) b);
  }

  public static Object modulo(Float a, Character b) {
    return ((float) a) % ((float) b);
  }

  public static Object equals(Float a, Character b) {
    return ((float) a) == ((float) b);
  }

  public static Object notequals(Float a, Character b) {
    return ((float) a) != ((float) b);
  }

  public static Object less(Float a, Character b) {
    return ((float) a) < ((float) b);
  }

  public static Object lessorequals(Float a, Character b) {
    return ((float) a) <= ((float) b);
  }

  public static Object more(Float a, Character b) {
    return ((float) a) > ((float) b);
  }

  public static Object moreorequals(Float a, Character b) {
    return ((float) a) >= ((float) b);
  }

  public static Object plus(Long a, Integer b) {
    return ((long) a) + ((long) b);
  }

  public static Object minus(Long a, Integer b) {
    return ((long) a) - ((long) b);
  }

  public static Object divide(Long a, Integer b) {
    return ((long) a) / ((long) b);
  }

  public static Object times(Long a, Integer b) {
    return ((long) a) * ((long) b);
  }

  public static Object modulo(Long a, Integer b) {
    return ((long) a) % ((long) b);
  }

  public static Object equals(Long a, Integer b) {
    return ((long) a) == ((long) b);
  }

  public static Object notequals(Long a, Integer b) {
    return ((long) a) != ((long) b);
  }

  public static Object less(Long a, Integer b) {
    return ((long) a) < ((long) b);
  }

  public static Object lessorequals(Long a, Integer b) {
    return ((long) a) <= ((long) b);
  }

  public static Object more(Long a, Integer b) {
    return ((long) a) > ((long) b);
  }

  public static Object moreorequals(Long a, Integer b) {
    return ((long) a) >= ((long) b);
  }

  public static Object plus(Double a, Integer b) {
    return ((double) a) + ((double) b);
  }

  public static Object minus(Double a, Integer b) {
    return ((double) a) - ((double) b);
  }

  public static Object divide(Double a, Integer b) {
    return ((double) a) / ((double) b);
  }

  public static Object times(Double a, Integer b) {
    return ((double) a) * ((double) b);
  }

  public static Object modulo(Double a, Integer b) {
    return ((double) a) % ((double) b);
  }

  public static Object equals(Double a, Integer b) {
    return ((double) a) == ((double) b);
  }

  public static Object notequals(Double a, Integer b) {
    return ((double) a) != ((double) b);
  }

  public static Object less(Double a, Integer b) {
    return ((double) a) < ((double) b);
  }

  public static Object lessorequals(Double a, Integer b) {
    return ((double) a) <= ((double) b);
  }

  public static Object more(Double a, Integer b) {
    return ((double) a) > ((double) b);
  }

  public static Object moreorequals(Double a, Integer b) {
    return ((double) a) >= ((double) b);
  }

  public static Object plus(Float a, Integer b) {
    return ((float) a) + ((float) b);
  }

  public static Object minus(Float a, Integer b) {
    return ((float) a) - ((float) b);
  }

  public static Object divide(Float a, Integer b) {
    return ((float) a) / ((float) b);
  }

  public static Object times(Float a, Integer b) {
    return ((float) a) * ((float) b);
  }

  public static Object modulo(Float a, Integer b) {
    return ((float) a) % ((float) b);
  }

  public static Object equals(Float a, Integer b) {
    return ((float) a) == ((float) b);
  }

  public static Object notequals(Float a, Integer b) {
    return ((float) a) != ((float) b);
  }

  public static Object less(Float a, Integer b) {
    return ((float) a) < ((float) b);
  }

  public static Object lessorequals(Float a, Integer b) {
    return ((float) a) <= ((float) b);
  }

  public static Object more(Float a, Integer b) {
    return ((float) a) > ((float) b);
  }

  public static Object moreorequals(Float a, Integer b) {
    return ((float) a) >= ((float) b);
  }

  public static Object plus(Double a, Long b) {
    return ((double) a) + ((double) b);
  }

  public static Object minus(Double a, Long b) {
    return ((double) a) - ((double) b);
  }

  public static Object divide(Double a, Long b) {
    return ((double) a) / ((double) b);
  }

  public static Object times(Double a, Long b) {
    return ((double) a) * ((double) b);
  }

  public static Object modulo(Double a, Long b) {
    return ((double) a) % ((double) b);
  }

  public static Object equals(Double a, Long b) {
    return ((double) a) == ((double) b);
  }

  public static Object notequals(Double a, Long b) {
    return ((double) a) != ((double) b);
  }

  public static Object less(Double a, Long b) {
    return ((double) a) < ((double) b);
  }

  public static Object lessorequals(Double a, Long b) {
    return ((double) a) <= ((double) b);
  }

  public static Object more(Double a, Long b) {
    return ((double) a) > ((double) b);
  }

  public static Object moreorequals(Double a, Long b) {
    return ((double) a) >= ((double) b);
  }

  public static Object plus(Float a, Long b) {
    return ((float) a) + ((float) b);
  }

  public static Object minus(Float a, Long b) {
    return ((float) a) - ((float) b);
  }

  public static Object divide(Float a, Long b) {
    return ((float) a) / ((float) b);
  }

  public static Object times(Float a, Long b) {
    return ((float) a) * ((float) b);
  }

  public static Object modulo(Float a, Long b) {
    return ((float) a) % ((float) b);
  }

  public static Object equals(Float a, Long b) {
    return ((float) a) == ((float) b);
  }

  public static Object notequals(Float a, Long b) {
    return ((float) a) != ((float) b);
  }

  public static Object less(Float a, Long b) {
    return ((float) a) < ((float) b);
  }

  public static Object lessorequals(Float a, Long b) {
    return ((float) a) <= ((float) b);
  }

  public static Object more(Float a, Long b) {
    return ((float) a) > ((float) b);
  }

  public static Object moreorequals(Float a, Long b) {
    return ((float) a) >= ((float) b);
  }

  public static Object plus(Float a, Double b) {
    return ((double) a) + ((double) b);
  }

  public static Object minus(Float a, Double b) {
    return ((double) a) - ((double) b);
  }

  public static Object divide(Float a, Double b) {
    return ((double) a) / ((double) b);
  }

  public static Object times(Float a, Double b) {
    return ((double) a) * ((double) b);
  }

  public static Object modulo(Float a, Double b) {
    return ((double) a) % ((double) b);
  }

  public static Object equals(Float a, Double b) {
    return ((double) a) == ((double) b);
  }

  public static Object notequals(Float a, Double b) {
    return ((double) a) != ((double) b);
  }

  public static Object less(Float a, Double b) {
    return ((double) a) < ((double) b);
  }

  public static Object lessorequals(Float a, Double b) {
    return ((double) a) <= ((double) b);
  }

  public static Object more(Float a, Double b) {
    return ((double) a) > ((double) b);
  }

  public static Object moreorequals(Float a, Double b) {
    return ((double) a) >= ((double) b);
  }

  public static Object equals(BigDecimal a, Integer b) {
    return (a).compareTo(new BigDecimal(b)) == 0;
  }

  public static Object equals(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) == 0;
  }

  public static Object notequals(BigDecimal a, Integer b) {
    return (a).compareTo(new BigDecimal(b)) != 0;
  }

  public static Object notequals(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) != 0;
  }

  public static Object less(BigDecimal a, Integer b) {
    return (a).compareTo(new BigDecimal(b)) < 0;
  }

  public static Object less(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) < 0;
  }

  public static Object lessorequals(BigDecimal a, Integer b) {
    return (a).compareTo(new BigDecimal(b)) <= 0;
  }

  public static Object lessorequals(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) <= 0;
  }

  public static Object more(BigDecimal a, Integer b) {
    return (a).compareTo(new BigDecimal(b)) > 0;
  }

  public static Object more(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) > 0;
  }

  public static Object moreorequals(BigDecimal a, Integer b) {
    return (a).compareTo(new BigDecimal(b)) >= 0;
  }

  public static Object moreorequals(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) >= 0;
  }

  public static Object plus(BigDecimal a, Integer b) {
    return (a).add(new BigDecimal(b));
  }

  public static Object plus(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).add(b);
  }

  public static Object minus(BigDecimal a, Integer b) {
    return (a).subtract(new BigDecimal(b));
  }

  public static Object minus(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).subtract(b);
  }

  public static Object times(BigDecimal a, Integer b) {
    return (a).multiply(new BigDecimal(b));
  }

  public static Object times(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).multiply(b);
  }

  public static Object divide(BigDecimal a, Integer b) {
    return (a).divide(new BigDecimal(b));
  }

  public static Object divide(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).divide(b);
  }

  public static Object modulo(BigDecimal a, Integer b) {
    return (a).remainder(new BigDecimal(b));
  }

  public static Object modulo(Integer a, BigDecimal b) {
    return (new BigDecimal(a)).remainder(b);
  }

  public static Object equals(BigDecimal a, Long b) {
    return (a).compareTo(new BigDecimal(b)) == 0;
  }

  public static Object equals(Long a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) == 0;
  }

  public static Object notequals(BigDecimal a, Long b) {
    return (a).compareTo(new BigDecimal(b)) != 0;
  }

  public static Object notequals(Long a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) != 0;
  }

  public static Object less(BigDecimal a, Long b) {
    return (a).compareTo(new BigDecimal(b)) < 0;
  }

  public static Object less(Long a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) < 0;
  }

  public static Object lessorequals(BigDecimal a, Long b) {
    return (a).compareTo(new BigDecimal(b)) <= 0;
  }

  public static Object lessorequals(Long a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) <= 0;
  }

  public static Object more(BigDecimal a, Long b) {
    return (a).compareTo(new BigDecimal(b)) > 0;
  }

  public static Object more(Long a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) > 0;
  }

  public static Object moreorequals(BigDecimal a, Long b) {
    return (a).compareTo(new BigDecimal(b)) >= 0;
  }

  public static Object moreorequals(Long a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) >= 0;
  }

  public static Object plus(BigDecimal a, Long b) {
    return (a).add(new BigDecimal(b));
  }

  public static Object plus(Long a, BigDecimal b) {
    return (new BigDecimal(a)).add(b);
  }

  public static Object minus(BigDecimal a, Long b) {
    return (a).subtract(new BigDecimal(b));
  }

  public static Object minus(Long a, BigDecimal b) {
    return (new BigDecimal(a)).subtract(b);
  }

  public static Object times(BigDecimal a, Long b) {
    return (a).multiply(new BigDecimal(b));
  }

  public static Object times(Long a, BigDecimal b) {
    return (new BigDecimal(a)).multiply(b);
  }

  public static Object divide(BigDecimal a, Long b) {
    return (a).divide(new BigDecimal(b));
  }

  public static Object divide(Long a, BigDecimal b) {
    return (new BigDecimal(a)).divide(b);
  }

  public static Object modulo(BigDecimal a, Long b) {
    return (a).remainder(new BigDecimal(b));
  }

  public static Object modulo(Long a, BigDecimal b) {
    return (new BigDecimal(a)).remainder(b);
  }

  public static Object equals(BigDecimal a, BigInteger b) {
    return (a).compareTo(new BigDecimal(b)) == 0;
  }

  public static Object equals(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) == 0;
  }

  public static Object notequals(BigDecimal a, BigInteger b) {
    return (a).compareTo(new BigDecimal(b)) != 0;
  }

  public static Object notequals(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) != 0;
  }

  public static Object less(BigDecimal a, BigInteger b) {
    return (a).compareTo(new BigDecimal(b)) < 0;
  }

  public static Object less(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) < 0;
  }

  public static Object lessorequals(BigDecimal a, BigInteger b) {
    return (a).compareTo(new BigDecimal(b)) <= 0;
  }

  public static Object lessorequals(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) <= 0;
  }

  public static Object more(BigDecimal a, BigInteger b) {
    return (a).compareTo(new BigDecimal(b)) > 0;
  }

  public static Object more(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) > 0;
  }

  public static Object moreorequals(BigDecimal a, BigInteger b) {
    return (a).compareTo(new BigDecimal(b)) >= 0;
  }

  public static Object moreorequals(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) >= 0;
  }

  public static Object plus(BigDecimal a, BigInteger b) {
    return (a).add(new BigDecimal(b));
  }

  public static Object plus(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).add(b);
  }

  public static Object minus(BigDecimal a, BigInteger b) {
    return (a).subtract(new BigDecimal(b));
  }

  public static Object minus(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).subtract(b);
  }

  public static Object times(BigDecimal a, BigInteger b) {
    return (a).multiply(new BigDecimal(b));
  }

  public static Object times(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).multiply(b);
  }

  public static Object divide(BigDecimal a, BigInteger b) {
    return (a).divide(new BigDecimal(b));
  }

  public static Object divide(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).divide(b);
  }

  public static Object modulo(BigDecimal a, BigInteger b) {
    return (a).remainder(new BigDecimal(b));
  }

  public static Object modulo(BigInteger a, BigDecimal b) {
    return (new BigDecimal(a)).remainder(b);
  }

  public static Object equals(BigDecimal a, Float b) {
    return (a).compareTo(new BigDecimal(b)) == 0;
  }

  public static Object equals(Float a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) == 0;
  }

  public static Object notequals(BigDecimal a, Float b) {
    return (a).compareTo(new BigDecimal(b)) != 0;
  }

  public static Object notequals(Float a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) != 0;
  }

  public static Object less(BigDecimal a, Float b) {
    return (a).compareTo(new BigDecimal(b)) < 0;
  }

  public static Object less(Float a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) < 0;
  }

  public static Object lessorequals(BigDecimal a, Float b) {
    return (a).compareTo(new BigDecimal(b)) <= 0;
  }

  public static Object lessorequals(Float a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) <= 0;
  }

  public static Object more(BigDecimal a, Float b) {
    return (a).compareTo(new BigDecimal(b)) > 0;
  }

  public static Object more(Float a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) > 0;
  }

  public static Object moreorequals(BigDecimal a, Float b) {
    return (a).compareTo(new BigDecimal(b)) >= 0;
  }

  public static Object moreorequals(Float a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) >= 0;
  }

  public static Object plus(BigDecimal a, Float b) {
    return (a).add(new BigDecimal(b));
  }

  public static Object plus(Float a, BigDecimal b) {
    return (new BigDecimal(a)).add(b);
  }

  public static Object minus(BigDecimal a, Float b) {
    return (a).subtract(new BigDecimal(b));
  }

  public static Object minus(Float a, BigDecimal b) {
    return (new BigDecimal(a)).subtract(b);
  }

  public static Object times(BigDecimal a, Float b) {
    return (a).multiply(new BigDecimal(b));
  }

  public static Object times(Float a, BigDecimal b) {
    return (new BigDecimal(a)).multiply(b);
  }

  public static Object divide(BigDecimal a, Float b) {
    return (a).divide(new BigDecimal(b));
  }

  public static Object divide(Float a, BigDecimal b) {
    return (new BigDecimal(a)).divide(b);
  }

  public static Object modulo(BigDecimal a, Float b) {
    return (a).remainder(new BigDecimal(b));
  }

  public static Object modulo(Float a, BigDecimal b) {
    return (new BigDecimal(a)).remainder(b);
  }

  public static Object equals(BigDecimal a, Double b) {
    return (a).compareTo(new BigDecimal(b)) == 0;
  }

  public static Object equals(Double a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) == 0;
  }

  public static Object notequals(BigDecimal a, Double b) {
    return (a).compareTo(new BigDecimal(b)) != 0;
  }

  public static Object notequals(Double a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) != 0;
  }

  public static Object less(BigDecimal a, Double b) {
    return (a).compareTo(new BigDecimal(b)) < 0;
  }

  public static Object less(Double a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) < 0;
  }

  public static Object lessorequals(BigDecimal a, Double b) {
    return (a).compareTo(new BigDecimal(b)) <= 0;
  }

  public static Object lessorequals(Double a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) <= 0;
  }

  public static Object more(BigDecimal a, Double b) {
    return (a).compareTo(new BigDecimal(b)) > 0;
  }

  public static Object more(Double a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) > 0;
  }

  public static Object moreorequals(BigDecimal a, Double b) {
    return (a).compareTo(new BigDecimal(b)) >= 0;
  }

  public static Object moreorequals(Double a, BigDecimal b) {
    return (new BigDecimal(a)).compareTo(b) >= 0;
  }

  public static Object plus(BigDecimal a, Double b) {
    return (a).add(new BigDecimal(b));
  }

  public static Object plus(Double a, BigDecimal b) {
    return (new BigDecimal(a)).add(b);
  }

  public static Object minus(BigDecimal a, Double b) {
    return (a).subtract(new BigDecimal(b));
  }

  public static Object minus(Double a, BigDecimal b) {
    return (new BigDecimal(a)).subtract(b);
  }

  public static Object times(BigDecimal a, Double b) {
    return (a).multiply(new BigDecimal(b));
  }

  public static Object times(Double a, BigDecimal b) {
    return (new BigDecimal(a)).multiply(b);
  }

  public static Object divide(BigDecimal a, Double b) {
    return (a).divide(new BigDecimal(b));
  }

  public static Object divide(Double a, BigDecimal b) {
    return (new BigDecimal(a)).divide(b);
  }

  public static Object modulo(BigDecimal a, Double b) {
    return (a).remainder(new BigDecimal(b));
  }

  public static Object modulo(Double a, BigDecimal b) {
    return (new BigDecimal(a)).remainder(b);
  }

  public static Object equals(BigDecimal a, BigDecimal b) {
    return (a).compareTo(b) == 0;
  }

  public static Object notequals(BigDecimal a, BigDecimal b) {
    return (a).compareTo(b) != 0;
  }

  public static Object less(BigDecimal a, BigDecimal b) {
    return (a).compareTo(b) < 0;
  }

  public static Object lessorequals(BigDecimal a, BigDecimal b) {
    return (a).compareTo(b) <= 0;
  }

  public static Object more(BigDecimal a, BigDecimal b) {
    return (a).compareTo(b) > 0;
  }

  public static Object moreorequals(BigDecimal a, BigDecimal b) {
    return (a).compareTo(b) >= 0;
  }

  public static Object plus(BigDecimal a, BigDecimal b) {
    return (a).add(b);
  }

  public static Object minus(BigDecimal a, BigDecimal b) {
    return (a).subtract(b);
  }

  public static Object times(BigDecimal a, BigDecimal b) {
    return (a).multiply(b);
  }

  public static Object divide(BigDecimal a, BigDecimal b) {
    return (a).divide(b);
  }

  public static Object modulo(BigDecimal a, BigDecimal b) {
    return (a).remainder(b);
  }

  public static Object equals(BigInteger a, Integer b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) == 0;
  }

  public static Object equals(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) == 0;
  }

  public static Object notequals(BigInteger a, Integer b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) != 0;
  }

  public static Object notequals(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) != 0;
  }

  public static Object less(BigInteger a, Integer b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) < 0;
  }

  public static Object less(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) < 0;
  }

  public static Object lessorequals(BigInteger a, Integer b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) <= 0;
  }

  public static Object lessorequals(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) <= 0;
  }

  public static Object more(BigInteger a, Integer b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) > 0;
  }

  public static Object more(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) > 0;
  }

  public static Object moreorequals(BigInteger a, Integer b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) >= 0;
  }

  public static Object moreorequals(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) >= 0;
  }

  public static Object plus(BigInteger a, Integer b) {
    return (a).add(BigInteger.valueOf(b.longValue()));
  }

  public static Object plus(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).add(b);
  }

  public static Object minus(BigInteger a, Integer b) {
    return (a).subtract(BigInteger.valueOf(b.longValue()));
  }

  public static Object minus(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).subtract(b);
  }

  public static Object times(BigInteger a, Integer b) {
    return (a).multiply(BigInteger.valueOf(b.longValue()));
  }

  public static Object times(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).multiply(b);
  }

  public static Object divide(BigInteger a, Integer b) {
    return (a).divide(BigInteger.valueOf(b.longValue()));
  }

  public static Object divide(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).divide(b);
  }

  public static Object modulo(BigInteger a, Integer b) {
    return (a).remainder(BigInteger.valueOf(b.longValue()));
  }

  public static Object modulo(Integer a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).remainder(b);
  }

  public static Object equals(BigInteger a, Long b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) == 0;
  }

  public static Object equals(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) == 0;
  }

  public static Object notequals(BigInteger a, Long b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) != 0;
  }

  public static Object notequals(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) != 0;
  }

  public static Object less(BigInteger a, Long b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) < 0;
  }

  public static Object less(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) < 0;
  }

  public static Object lessorequals(BigInteger a, Long b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) <= 0;
  }

  public static Object lessorequals(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) <= 0;
  }

  public static Object more(BigInteger a, Long b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) > 0;
  }

  public static Object more(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) > 0;
  }

  public static Object moreorequals(BigInteger a, Long b) {
    return (a).compareTo(BigInteger.valueOf(b.longValue())) >= 0;
  }

  public static Object moreorequals(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).compareTo(b) >= 0;
  }

  public static Object plus(BigInteger a, Long b) {
    return (a).add(BigInteger.valueOf(b.longValue()));
  }

  public static Object plus(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).add(b);
  }

  public static Object minus(BigInteger a, Long b) {
    return (a).subtract(BigInteger.valueOf(b.longValue()));
  }

  public static Object minus(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).subtract(b);
  }

  public static Object times(BigInteger a, Long b) {
    return (a).multiply(BigInteger.valueOf(b.longValue()));
  }

  public static Object times(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).multiply(b);
  }

  public static Object divide(BigInteger a, Long b) {
    return (a).divide(BigInteger.valueOf(b.longValue()));
  }

  public static Object divide(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).divide(b);
  }

  public static Object modulo(BigInteger a, Long b) {
    return (a).remainder(BigInteger.valueOf(b.longValue()));
  }

  public static Object modulo(Long a, BigInteger b) {
    return (BigInteger.valueOf(a.longValue())).remainder(b);
  }

  public static Object equals(BigInteger a, BigInteger b) {
    return (a).compareTo(b) == 0;
  }

  public static Object notequals(BigInteger a, BigInteger b) {
    return (a).compareTo(b) != 0;
  }

  public static Object less(BigInteger a, BigInteger b) {
    return (a).compareTo(b) < 0;
  }

  public static Object lessorequals(BigInteger a, BigInteger b) {
    return (a).compareTo(b) <= 0;
  }

  public static Object more(BigInteger a, BigInteger b) {
    return (a).compareTo(b) > 0;
  }

  public static Object moreorequals(BigInteger a, BigInteger b) {
    return (a).compareTo(b) >= 0;
  }

  public static Object plus(BigInteger a, BigInteger b) {
    return (a).add(b);
  }

  public static Object minus(BigInteger a, BigInteger b) {
    return (a).subtract(b);
  }

  public static Object times(BigInteger a, BigInteger b) {
    return (a).multiply(b);
  }

  public static Object divide(BigInteger a, BigInteger b) {
    return (a).divide(b);
  }

  public static Object modulo(BigInteger a, BigInteger b) {
    return (a).remainder(b);
  }

  public static Object equals(BigInteger a, Float b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) == 0;
  }

  public static Object equals(Float a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) == 0;
  }

  public static Object notequals(BigInteger a, Float b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) != 0;
  }

  public static Object notequals(Float a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) != 0;
  }

  public static Object less(BigInteger a, Float b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) < 0;
  }

  public static Object less(Float a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) < 0;
  }

  public static Object lessorequals(BigInteger a, Float b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) <= 0;
  }

  public static Object lessorequals(Float a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) <= 0;
  }

  public static Object more(BigInteger a, Float b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) > 0;
  }

  public static Object more(Float a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) > 0;
  }

  public static Object moreorequals(BigInteger a, Float b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) >= 0;
  }

  public static Object moreorequals(Float a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) >= 0;
  }

  public static Object plus(BigInteger a, Float b) {
    return (new BigDecimal(a)).add(new BigDecimal(b));
  }

  public static Object plus(Float a, BigInteger b) {
    return (new BigDecimal(a)).add(new BigDecimal(b));
  }

  public static Object minus(BigInteger a, Float b) {
    return (new BigDecimal(a)).subtract(new BigDecimal(b));
  }

  public static Object minus(Float a, BigInteger b) {
    return (new BigDecimal(a)).subtract(new BigDecimal(b));
  }

  public static Object times(BigInteger a, Float b) {
    return (new BigDecimal(a)).multiply(new BigDecimal(b));
  }

  public static Object times(Float a, BigInteger b) {
    return (new BigDecimal(a)).multiply(new BigDecimal(b));
  }

  public static Object divide(BigInteger a, Float b) {
    return (new BigDecimal(a)).divide(new BigDecimal(b));
  }

  public static Object divide(Float a, BigInteger b) {
    return (new BigDecimal(a)).divide(new BigDecimal(b));
  }

  public static Object modulo(BigInteger a, Float b) {
    return (new BigDecimal(a)).remainder(new BigDecimal(b));
  }

  public static Object modulo(Float a, BigInteger b) {
    return (new BigDecimal(a)).remainder(new BigDecimal(b));
  }

  public static Object equals(BigInteger a, Double b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) == 0;
  }

  public static Object equals(Double a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) == 0;
  }

  public static Object notequals(BigInteger a, Double b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) != 0;
  }

  public static Object notequals(Double a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) != 0;
  }

  public static Object less(BigInteger a, Double b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) < 0;
  }

  public static Object less(Double a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) < 0;
  }

  public static Object lessorequals(BigInteger a, Double b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) <= 0;
  }

  public static Object lessorequals(Double a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) <= 0;
  }

  public static Object more(BigInteger a, Double b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) > 0;
  }

  public static Object more(Double a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) > 0;
  }

  public static Object moreorequals(BigInteger a, Double b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) >= 0;
  }

  public static Object moreorequals(Double a, BigInteger b) {
    return (new BigDecimal(a)).compareTo(new BigDecimal(b)) >= 0;
  }

  public static Object plus(BigInteger a, Double b) {
    return (new BigDecimal(a)).add(new BigDecimal(b));
  }

  public static Object plus(Double a, BigInteger b) {
    return (new BigDecimal(a)).add(new BigDecimal(b));
  }

  public static Object minus(BigInteger a, Double b) {
    return (new BigDecimal(a)).subtract(new BigDecimal(b));
  }

  public static Object minus(Double a, BigInteger b) {
    return (new BigDecimal(a)).subtract(new BigDecimal(b));
  }

  public static Object times(BigInteger a, Double b) {
    return (new BigDecimal(a)).multiply(new BigDecimal(b));
  }

  public static Object times(Double a, BigInteger b) {
    return (new BigDecimal(a)).multiply(new BigDecimal(b));
  }

  public static Object divide(BigInteger a, Double b) {
    return (new BigDecimal(a)).divide(new BigDecimal(b));
  }

  public static Object divide(Double a, BigInteger b) {
    return (new BigDecimal(a)).divide(new BigDecimal(b));
  }

  public static Object modulo(BigInteger a, Double b) {
    return (new BigDecimal(a)).remainder(new BigDecimal(b));
  }

  public static Object modulo(Double a, BigInteger b) {
    return (new BigDecimal(a)).remainder(new BigDecimal(b));
  }

  // END GENERATED

  // arithmetic fallbacks .............................................................................................

  public static Object plus(String a, String b) {
    return a + b;
  }

  public static Object plus_fallback(Object a, Object b) {
    if (isNotNullAndString(a) || isNotNullAndString(b)) {
      return String.valueOf(a) + b;
    }
    return reject(a, b, "+");
  }

  public static Object times_fallback(Object a, Object b) {
    if (isInteger(a) && isString(b)) {
      return repeat((String) b, (Integer) a);
    }
    if (isString(a) && isInteger(b)) {
      return repeat((String) a, (Integer) b);
    }
    return reject(a, b, "*");
  }

  private static String repeat(String string, int n) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < n; i++) {
      builder.append(string);
    }
    return builder.toString();
  }

  // comparisons fallback .............................................................................................

  public static Object equals_fallback(Object a, Object b) {
    return Objects.equals(a, b);
  }

  public static Object notequals_fallback(Object a, Object b) {
    return !Objects.equals(a, b);
  }

  @SuppressWarnings("unchecked")
  public static Object less_fallback(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) < 0;
    }
    return reject(a, b, "<");
  }

  @SuppressWarnings("unchecked")
  public static Object lessorequals_fallback(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) <= 0;
    }
    return reject(a, b, "<=");
  }

  @SuppressWarnings("unchecked")
  public static Object more_fallback(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) > 0;
    }
    return reject(a, b, ">");
  }

  @SuppressWarnings("unchecked")
  public static Object moreorequals_fallback(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) >= 0;
    }
    return reject(a, b, ">=");
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
    throw new IllegalArgumentException(
        message("invalid_unary_operator", symbol, a.getClass().getName()));
  }

  private static Object reject(Object a, Object b, String symbol) throws IllegalArgumentException {
    throw new IllegalArgumentException(
        message("invalid_binary_operator", symbol, a.getClass().getName(), b.getClass().getName()));
  }
}
