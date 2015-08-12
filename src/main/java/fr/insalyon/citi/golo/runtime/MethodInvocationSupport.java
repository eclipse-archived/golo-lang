/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.runtime;

import gololang.DynamicObject;

import java.lang.invoke.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.WeakHashMap;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;


public class MethodInvocationSupport {

  /*
   * This code is heavily inspired from the inline cache construction from
   * Remi Forax's JSR292 cookbooks.
   */

  static final class InlineCache extends MutableCallSite {

    static final int MEGAMORPHIC_THRESHOLD = 5;

    final MethodHandles.Lookup callerLookup;
    final String name;
    final boolean nullSafeGuarded;
    final String[] argumentNames;


    int depth = 0;
    WeakHashMap<Class, MethodHandle> vtable;

    InlineCache(Lookup callerLookup, String name, MethodType type, boolean nullSafeGuarded, String... argumentNames) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
      this.nullSafeGuarded = nullSafeGuarded;
      this.argumentNames = argumentNames;
    }

    boolean isMegaMorphic() {
      return depth > MEGAMORPHIC_THRESHOLD;
    }
  }

  private static final MethodHandle CLASS_GUARD;
  private static final MethodHandle FALLBACK;
  private static final MethodHandle VTABLE_LOOKUP;

  private static final HashSet<String> DYNAMIC_OBJECT_RESERVED_METHOD_NAMES = new HashSet<String>() {
    {
      add("get");
      add("define");
      add("undefine");
      add("mixin");
      add("copy");
      add("freeze");
      add("properties");
      add("invoker");
      add("hasMethod");
      add("fallback");
    }
  };

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      CLASS_GUARD = lookup.findStatic(
          MethodInvocationSupport.class,
          "classGuard",
          methodType(boolean.class, Class.class, Object.class));

      FALLBACK = lookup.findStatic(
          MethodInvocationSupport.class,
          "fallback",
          methodType(Object.class, InlineCache.class, Object[].class));

      VTABLE_LOOKUP = lookup.findStatic(
          MethodInvocationSupport.class,
          "vtableLookup",
          methodType(MethodHandle.class, InlineCache.class, Object[].class));

    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static CallSite bootstrap(Lookup caller, String name, MethodType type,  Object... bsmArgs) {
    boolean nullSafeGuarded = ((int)bsmArgs[0]) == 1;
    String[] argumentNames = new String[bsmArgs.length - 1];
    for (int i = 0; i < bsmArgs.length -1; i++) {
      argumentNames[i] = (String) bsmArgs[i+1];
    }
    InlineCache callSite = new InlineCache(caller, name, type, nullSafeGuarded, argumentNames);
    MethodHandle fallbackHandle = FALLBACK
        .bindTo(callSite)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  public static boolean classGuard(Class<?> expected, Object receiver) {
    return receiver.getClass() == expected;
  }

  public static MethodHandle vtableLookup(InlineCache inlineCache, Object[] args) {
    Class<?> receiverClass = args[0].getClass();
    MethodHandle target = inlineCache.vtable.get(receiverClass);
    if (target == null) {
      target = lookupTarget(receiverClass, inlineCache, args);
      inlineCache.vtable.put(receiverClass, target);
    }
    return target;
  }

  private static MethodHandle lookupTarget(Class<?> receiverClass, InlineCache inlineCache, Object[] args) {
    if (receiverClass.isArray()) {
      return new ArrayMethodFinder(inlineCache, receiverClass, args).find();
    }
    if (isCallOnDynamicObject(inlineCache, args[0])) {
      DynamicObject dynamicObject = (DynamicObject) args[0];
      return dynamicObject.invoker(inlineCache.name, inlineCache.type());
    } else {
      return findTarget(receiverClass, inlineCache, args);
    }
  }

  public static Object fallback(InlineCache inlineCache, Object[] args) throws Throwable {

    if (inlineCache.isMegaMorphic()) {
      return installVTableDispatch(inlineCache, args);
    }

    if (args[0] == null) {
      if (shouldReturnNull(inlineCache, args[0])) {
        return null;
      } else {
        throw new NullPointerException("On method: " + inlineCache.name + " " + inlineCache.type().dropParameterTypes(0, 1));
      }
    }

    Class<?> receiverClass = args[0].getClass();
    MethodHandle target = lookupTarget(receiverClass, inlineCache, args);

    if (target == null) {
      InlineCache fallbackCallSite = new InlineCache(
          inlineCache.callerLookup,
          "fallback",
          methodType(Object.class, Object.class, Object.class, Object[].class),
          false);
      Object[] fallbackArgs = new Object[] {
          args[0],
          inlineCache.name,
          Arrays.copyOfRange(args,1,args.length)
      };
      target = lookupTarget(receiverClass, fallbackCallSite, fallbackArgs);
      if (target != null) {
        return fallback(fallbackCallSite, fallbackArgs);
      } else {
        throw new NoSuchMethodError(receiverClass + "::" + inlineCache.name);
      }
    }

    MethodHandle guard = CLASS_GUARD.bindTo(receiverClass);
    MethodHandle fallback = inlineCache.getTarget();
    MethodHandle root = guardWithTest(guard, target, fallback);
    if (inlineCache.nullSafeGuarded) {
      root = makeNullSafeGuarded(root);
    }
    inlineCache.setTarget(root);
    inlineCache.depth = inlineCache.depth + 1;
    return target.invokeWithArguments(args);
  }

  private static MethodHandle makeNullSafeGuarded(MethodHandle root) {
    MethodHandle catchThenNull = dropArguments(constant(Object.class, null), 0, NullPointerException.class);
    root = catchException(root, NullPointerException.class, catchThenNull);
    return root;
  }

  private static boolean shouldReturnNull(InlineCache inlineCache, Object arg) {
    return (arg == null) && inlineCache.nullSafeGuarded;
  }

  private static Object installVTableDispatch(InlineCache inlineCache, Object[] args) throws Throwable {
    if (inlineCache.vtable == null) {
      inlineCache.vtable = new WeakHashMap<>();
    }
    MethodHandle lookup = VTABLE_LOOKUP
        .bindTo(inlineCache)
        .asCollector(Object[].class, args.length);
    MethodHandle exactInvoker = exactInvoker(inlineCache.type());
    MethodHandle vtableTarget = foldArguments(exactInvoker, lookup);
    if (inlineCache.nullSafeGuarded) {
      vtableTarget = makeNullSafeGuarded(vtableTarget);
    }
    inlineCache.setTarget(vtableTarget);
    if (shouldReturnNull(inlineCache, args[0])) {
      return null;
    }
    return vtableTarget.invokeWithArguments(args);
  }

  private static boolean isCallOnDynamicObject(InlineCache inlineCache, Object arg) {
    return (arg instanceof DynamicObject) && !(DYNAMIC_OBJECT_RESERVED_METHOD_NAMES.contains(inlineCache.name));
  }

  private static MethodHandle findTarget(Class<?> receiverClass, InlineCache inlineCache, Object[] args) {
    MethodHandle target;

    // NOTE: magic for accessors and mutators would go here...

    target = new RegularMethodFinder(inlineCache, receiverClass, args).find();
    if (target != null) { return target; }

    target = new AugmentationMethodFinder(inlineCache, receiverClass, args).find();
    if (target != null) { return target; }

    return null;
  }
}
