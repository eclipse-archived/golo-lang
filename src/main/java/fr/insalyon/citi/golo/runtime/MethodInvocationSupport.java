/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

import gololang.DynamicObject;

import java.lang.invoke.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static fr.insalyon.citi.golo.runtime.MethodInvocationSupport.InlineCache.State.DYNAMIC_OBJECT;
import static fr.insalyon.citi.golo.runtime.MethodInvocationSupport.InlineCache.State.POLYMORPHIC;
import static fr.insalyon.citi.golo.runtime.TypeMatching.*;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;
import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.copyOfRange;

public class MethodInvocationSupport {

  /*
   * This code is heavily inspired from the inline cache construction from
   * Remi Forax's JSR292 cookbooks.
   */

  static class InlineCache extends MutableCallSite {

    static final int MEGAMORPHIC_THRESHOLD = 5;

    static enum State {
      DYNAMIC_OBJECT, POLYMORPHIC
    }

    final MethodHandles.Lookup callerLookup;
    final String name;
    final boolean nullSafeGuarded;

    int depth = 0;
    State state = POLYMORPHIC;
    MethodHandle fallback;
    HashMap<Class, MethodHandle> vtable;

    InlineCache(Lookup callerLookup, String name, MethodType type, boolean nullSafeGuarded) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
      this.nullSafeGuarded = nullSafeGuarded;
    }

    boolean isMegaMorphic() {
      return depth > MEGAMORPHIC_THRESHOLD;
    }

    void resetWith(MethodHandle target) {
      setTarget(target);
    }
  }

  private static final MethodHandle CLASS_GUARD;
  private static final MethodHandle INSTANCE_GUARD;
  private static final MethodHandle FALLBACK;
  private static final MethodHandle VTABLE_LOOKUP;
  private static final MethodHandle NOT_DYNAMIC_OBJECT;

  private static final Set<String> DYNAMIC_OBJECT_RESERVED_METHOD_NAMES = new HashSet<String>() {
    {
      add("get");
      add("plug");
      add("define");
      add("undefine");
      add("mixin");
      add("copy");
      add("freeze");
      add("properties");
    }
  };

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      CLASS_GUARD = lookup.findStatic(
          MethodInvocationSupport.class,
          "classGuard",
          methodType(boolean.class, Class.class, Object.class));

      INSTANCE_GUARD = lookup.findStatic(
          MethodInvocationSupport.class,
          "instanceGuard",
          methodType(boolean.class, Object.class, Object.class));

      FALLBACK = lookup.findStatic(
          MethodInvocationSupport.class,
          "fallback",
          methodType(Object.class, InlineCache.class, Object[].class));

      VTABLE_LOOKUP = lookup.findStatic(
          MethodInvocationSupport.class,
          "vtableLookup",
          methodType(MethodHandle.class, InlineCache.class, Object[].class));

      NOT_DYNAMIC_OBJECT = lookup.findStatic(
          MethodInvocationSupport.class,
          "notDynamicObject",
          methodType(boolean.class, Object.class));

    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static CallSite bootstrap(Lookup caller, String name, MethodType type, int nullSafeGuarded) {
    InlineCache callSite = new InlineCache(caller, name, type, (nullSafeGuarded != 0));
    MethodHandle fallbackHandle = FALLBACK
        .bindTo(callSite)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    callSite.fallback = fallbackHandle;
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  public static boolean classGuard(Class<?> expected, Object receiver) {
    return receiver.getClass() == expected;
  }

  public static boolean instanceGuard(Object expected, Object receiver) {
    return expected == receiver;
  }

  public static boolean notDynamicObject(Object receiver) {
    return receiver.getClass() != DynamicObject.class;
  }

  public static MethodHandle vtableLookup(InlineCache inlineCache, Object[] args) {
    Class<?> receiverClass = args[0].getClass();
    MethodHandle target = inlineCache.vtable.get(receiverClass);
    if (target == null) {
      target = findTarget(receiverClass, inlineCache, args);
      inlineCache.vtable.put(receiverClass, target);
    }
    return target;
  }

  public static Object fallback(InlineCache inlineCache, Object[] args) throws Throwable {

    if (isCallOnDynamicObject(inlineCache, args[0])) {
      return installDynamicObjectDispatch(inlineCache, args);
    }

    if (inlineCache.isMegaMorphic()) {
      return installVTableDispatch(inlineCache, args);
    }

    if (shouldReturnNull(inlineCache, args[0])) {
      return null;
    }

    Class<?> receiverClass = args[0].getClass();
    MethodHandle target = findTarget(receiverClass, inlineCache, args);

    MethodHandle guard = CLASS_GUARD.bindTo(receiverClass);
    MethodHandle fallback = (inlineCache.state == POLYMORPHIC) ? inlineCache.getTarget() : inlineCache.fallback;
    MethodHandle root = guardWithTest(guard, target, fallback);
    if (inlineCache.nullSafeGuarded) {
      root = makeNullSafeGuarded(root);
    }
    inlineCache.setTarget(root);
    inlineCache.state = POLYMORPHIC;
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
      inlineCache.vtable = new HashMap<>();
    }
    MethodHandle lookup = VTABLE_LOOKUP
        .bindTo(inlineCache)
        .asCollector(Object[].class, args.length);
    MethodHandle exactInvoker = exactInvoker(inlineCache.type());
    MethodHandle vtableTarget = foldArguments(exactInvoker, lookup);
    MethodHandle gwt = guardWithTest(NOT_DYNAMIC_OBJECT, vtableTarget, inlineCache.fallback);
    if (inlineCache.nullSafeGuarded) {
      gwt = makeNullSafeGuarded(gwt);
    }
    inlineCache.setTarget(gwt);
    if (shouldReturnNull(inlineCache, args[0])) {
      return null;
    }
    return vtableTarget.invokeWithArguments(args);
  }

  private static Object installDynamicObjectDispatch(InlineCache inlineCache, Object[] args) throws Throwable {
    DynamicObject dynamicObject = (DynamicObject) args[0];
    MethodHandle target = dynamicObject.plug(inlineCache.name, inlineCache.type(), inlineCache.fallback);
    MethodHandle guard = INSTANCE_GUARD.bindTo(dynamicObject);
    MethodHandle root = guardWithTest(guard, target, inlineCache.fallback);
    inlineCache.state = DYNAMIC_OBJECT;
    inlineCache.resetWith(root);
    return target.invokeWithArguments(args);
  }

  private static boolean isCallOnDynamicObject(InlineCache inlineCache, Object arg) {
    return (arg instanceof DynamicObject) && !(DYNAMIC_OBJECT_RESERVED_METHOD_NAMES.contains(inlineCache.name));
  }

  private static MethodHandle findTarget(Class<?> receiverClass, InlineCache inlineCache, Object[] args) {
    MethodHandle target;
    MethodType type = inlineCache.type();
    boolean makeAccessible = !isPublic(receiverClass.getModifiers());

    if (receiverClass.isArray()) {
      return findArraySpecialMethod(receiverClass, inlineCache, args, type);
    }

    Object searchResult = findMethodOrField(receiverClass, inlineCache.name, type.parameterArray(), args);
    if (searchResult != null) {
      try {
        if (searchResult.getClass() == Method.class) {
          Method method = (Method) searchResult;
          if (makeAccessible) {
            method.setAccessible(true);
          }
          target = inlineCache.callerLookup.unreflect(method).asType(type);
        } else {
          Field field = (Field) searchResult;
          if (makeAccessible) {
            field.setAccessible(true);
          }
          if (args.length == 1) {
            target = inlineCache.callerLookup.unreflectGetter(field).asType(type);
          } else {
            target = inlineCache.callerLookup.unreflectSetter(field);
            target = filterReturnValue(target, constant(receiverClass, args[0])).asType(type);
          }
        }
        return target;
      } catch (IllegalAccessException ignored) {
        /* We need to give augmentations a chance, as IllegalAccessException can be noise in our resolution.
         * Example: augmenting HashSet with a map function.
         *  java.lang.IllegalAccessException: member is private: java.util.HashSet.map/java.util.HashMap/putField
         */
      }
    }

    target = findInAugmentations(receiverClass, inlineCache);
    if (target != null) {
      return target;
    }
    throw new NoSuchMethodError(receiverClass + "::" + inlineCache.name);
  }

  private static MethodHandle findArraySpecialMethod(Class<?> receiverClass, InlineCache inlineCache, Object[] args, MethodType type) {
    switch (inlineCache.name) {
      case "get":
        if (args.length != 2) {
          throw new UnsupportedOperationException("get on arrays takes 1 parameter");
        }
        return MethodHandles.arrayElementGetter(receiverClass).asType(type);
      case "set":
        if (args.length != 3) {
          throw new UnsupportedOperationException("set on arrays takes 2 parameters");
        }
        return MethodHandles.arrayElementSetter(receiverClass).asType(type);
      case "length":
        if (args.length != 1) {
          throw new UnsupportedOperationException("length on arrays takes no parameters");
        }
        try {
          return inlineCache.callerLookup.findStatic(
              Array.class, "getLength", methodType(int.class, Object.class)).asType(type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
          throw new Error(e);
        }
      case "iterator":
        if (args.length != 1) {
          throw new UnsupportedOperationException("iterator on arrays takes no parameters");
        }
        try {
          return inlineCache.callerLookup.findConstructor(
              PrimitiveArrayIterator.class, methodType(void.class, Object[].class)).asType(type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
          throw new Error(e);
        }
      case "toString":
        if (args.length != 1) {
          throw new UnsupportedOperationException("toString on arrays takes no parameters");
        }
        try {
          return inlineCache.callerLookup.findStatic(
              Arrays.class, "toString", methodType(String.class, Object[].class)).asType(type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
          throw new Error(e);
        }
      case "asList":
        if (args.length != 1) {
          throw new UnsupportedOperationException("toString on arrays takes no parameters");
        }
        try {
          return inlineCache.callerLookup.findStatic(
              Arrays.class, "asList", methodType(List.class, Object[].class)).asType(type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
          throw new Error(e);
        }
      case "equals":
        if (args.length != 2) {
          throw new UnsupportedOperationException("toString on arrays takes 1 parameter");
        }
        try {
          return inlineCache.callerLookup.findStatic(
              Arrays.class, "equals", methodType(boolean.class, Object[].class, Object[].class)).asType(type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
          throw new Error(e);
        }
      default:
        throw new UnsupportedOperationException(inlineCache.name + " is not supported on arrays");
    }
  }

  private static Object findMethodOrField(Class<?> receiverClass, String name, Class<?>[] argumentTypes, Object[] args) {

    List<Method> candidates = new LinkedList<>();
    for (Method method : receiverClass.getMethods()) {
      if (isCandidateMethod(name, method)) {
        candidates.add(method);
      }
    }

    if (candidates.size() == 1) {
      return candidates.get(0);
    }

    if (!candidates.isEmpty()) {
      for (Method method : candidates) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] argsWithoutReceiver = copyOfRange(args, 1, args.length);
        if (haveSameNumberOfArguments(argsWithoutReceiver, parameterTypes) || haveEnoughArgumentsForVarargs(argsWithoutReceiver, method, parameterTypes)) {
          if (canAssign(parameterTypes, argsWithoutReceiver, method.isVarArgs())) {
            return method;
          }
        }
      }
    }

    if (argumentTypes.length <= 2) {
      for (Field field : receiverClass.getDeclaredFields()) {
        if (isMatchingField(name, field)) {
          return field;
        }
      }
      for (Field field : receiverClass.getFields()) {
        if (isMatchingField(name, field)) {
          return field;
        }
      }
    }

    return null;
  }

  private static MethodHandle findInAugmentations(Class<?> receiverClass, InlineCache inlineCache) {
    Class<?> callerClass = inlineCache.callerLookup.lookupClass();
    String name = inlineCache.name;
    MethodType type = inlineCache.type();
    Lookup lookup = inlineCache.callerLookup;
    final int arity = inlineCache.type().parameterCount();

    ClassLoader classLoader = callerClass.getClassLoader();
    for (String augmentation : Module.augmentations(callerClass)) {
      try {
        Class<?> augmentedClass = classLoader.loadClass(augmentation);
        if (augmentedClass.isAssignableFrom(receiverClass)) {
          Class<?> augmentClass = classLoader.loadClass(augmentClassName(callerClass, augmentedClass));
          for (Method method : augmentClass.getMethods()) {
            if (isCandidateMethod(name, method) && augmentMethodMatches(arity, method)) {
              return lookup.unreflect(method).asType(type);
            }
          }
        }
      } catch (ClassNotFoundException | IllegalAccessException ignored) {
      }
    }

    // TODO: refactor the lookups above and below
    for (String importSymbol : Module.imports(callerClass)) {
      try {
        Class<?> importClass = classLoader.loadClass(importSymbol);
        for (String augmentation : Module.augmentations(importClass)) {
          try {
            Class<?> augmentedClass = classLoader.loadClass(augmentation);
            if (augmentedClass.isAssignableFrom(receiverClass)) {
              Class<?> augmentClass = classLoader.loadClass(augmentClassName(importClass, augmentedClass));
              for (Method method : augmentClass.getMethods()) {
                if (isCandidateMethod(name, method) && augmentMethodMatches(arity, method)) {
                  return lookup.unreflect(method).asType(type);
                }
              }
            }
          } catch (ClassNotFoundException | IllegalAccessException ignored) {
          }
        }
      } catch (ClassNotFoundException ignored) {
      }
    }

    return null;
  }

  private static boolean augmentMethodMatches(int arity, Method method) {
    int parameterCount = method.getParameterTypes().length;
    return (parameterCount == arity) || (method.isVarArgs() && (parameterCount <= arity));
  }

  private static String augmentClassName(Class<?> moduleClass, Class<?> augmentedClass) {
    return moduleClass.getName() + "$" + augmentedClass.getName().replace('.', '$');
  }

  private static boolean isMatchingField(String name, Field field) {
    return field.getName().equals(name) && !isStatic(field.getModifiers());
  }

  private static boolean isCandidateMethod(String name, Method method) {
    return method.getName().equals(name) && isPublic(method.getModifiers()) && !isAbstract(method.getModifiers());
  }

}
