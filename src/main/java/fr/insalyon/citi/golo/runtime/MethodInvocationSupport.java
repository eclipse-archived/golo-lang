package fr.insalyon.citi.golo.runtime;

import gololang.DynamicObject;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    static enum State {
      DYNAMIC_OBJECT, POLYMORPHIC
    }

    final MethodHandles.Lookup callerLookup;
    final String name;

    int depth = 0;
    State state = POLYMORPHIC;
    MethodHandle fallback;

    InlineCache(MethodHandles.Lookup callerLookup, String name, MethodType type) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
    }

    boolean isMegaMorphic() {
      return false;
      // TODO: check megamorphic fallback strategies
      // return depth >= 5;
    }

    void resetWith(MethodHandle target) {
      depth = 0;
      setTarget(target);
    }
  }

  private static final MethodHandle CLASS_GUARD;
  private static final MethodHandle INSTANCE_GUARD;
  private static final MethodHandle FALLBACK;

  private static final Set<String> DYNAMIC_OBJECT_RESERVED_METHOD_NAMES = new HashSet<String>() {
    {
      add("get");
      add("plug");
      add("define");
      add("undefine");
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

    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) {
    InlineCache callSite = new InlineCache(caller, name, type);
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

  public static Object fallback(InlineCache inlineCache, Object[] args) throws Throwable {

    if (isCallOnDynamicObject(inlineCache, args[0])) {
      DynamicObject dynamicObject = (DynamicObject) args[0];
      MethodHandle target = dynamicObject.plug(inlineCache.name, inlineCache.type(), inlineCache.fallback);
      MethodHandle guard = INSTANCE_GUARD.bindTo(dynamicObject);
      MethodHandle root = guardWithTest(guard, target, inlineCache.fallback);
      inlineCache.state = DYNAMIC_OBJECT;
      inlineCache.resetWith(root);
      return target.invokeWithArguments(args);
    }

    Class<?> receiverClass = args[0].getClass();
    MethodHandle target = findTarget(receiverClass, inlineCache, args);

    if (inlineCache.isMegaMorphic()) {
      return target.invokeWithArguments(args);
    }

    MethodHandle guard = CLASS_GUARD.bindTo(receiverClass);
    MethodHandle fallback = (inlineCache.state == POLYMORPHIC) ? inlineCache.getTarget() : inlineCache.fallback;
    MethodHandle root = guardWithTest(guard, target, fallback);
    inlineCache.setTarget(root);
    inlineCache.state = POLYMORPHIC;
    inlineCache.depth = inlineCache.depth + 1;
    return target.invokeWithArguments(args);
  }

  private static boolean isCallOnDynamicObject(InlineCache inlineCache, Object arg) {
    return (arg instanceof DynamicObject) && !(DYNAMIC_OBJECT_RESERVED_METHOD_NAMES.contains(inlineCache.name));
  }

  private static MethodHandle findTarget(Class<?> receiverClass, InlineCache inlineCache, Object[] args) {
    MethodHandle target;
    MethodType type = inlineCache.type();
    boolean makeAccessible = !isPublic(receiverClass.getModifiers());

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
        /* We need to give pimps a chance, as IllegalAccessException can be noise in our resolution.
         * Example: pimping HashSet with a map function.
         *  java.lang.IllegalAccessException: member is private: java.util.HashSet.map/java.util.HashMap/putField
         */
      }
    }

    target = findInPimps(receiverClass, inlineCache);
    if (target != null) {
      return target;
    }
    throw new NoSuchMethodError(receiverClass + "::" + inlineCache.name);
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

  private static MethodHandle findInPimps(Class<?> receiverClass, InlineCache inlineCache) {
    Class<?> callerClass = inlineCache.callerLookup.lookupClass();
    String name = inlineCache.name;
    MethodType type = inlineCache.type();
    Lookup lookup = inlineCache.callerLookup;
    final int arity = inlineCache.type().parameterCount();

    ClassLoader classLoader = callerClass.getClassLoader();
    for (String pimp : Module.pimps(callerClass)) {
      try {
        Class<?> pimpedClass = classLoader.loadClass(pimp);
        if (pimpedClass.isAssignableFrom(receiverClass)) {
          Class<?> pimpClass = classLoader.loadClass(pimpClassName(callerClass, pimpedClass));
          for (Method method : pimpClass.getMethods()) {
            if (isCandidateMethod(name, method) && pimpMethodMatches(arity, method)) {
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
        for (String pimp : Module.pimps(importClass)) {
          try {
            Class<?> pimpedClass = classLoader.loadClass(pimp);
            if (pimpedClass.isAssignableFrom(receiverClass)) {
              Class<?> pimpClass = classLoader.loadClass(pimpClassName(importClass, pimpedClass));
              for (Method method : pimpClass.getMethods()) {
                if (isCandidateMethod(name, method) && pimpMethodMatches(arity, method)) {
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

  private static boolean pimpMethodMatches(int arity, Method method) {
    int parameterCount = method.getParameterTypes().length;
    return (parameterCount == arity) || (method.isVarArgs() && (parameterCount <= arity));
  }

  private static String pimpClassName(Class<?> moduleClass, Class<?> pimpedClass) {
    return moduleClass.getName() + "$" + pimpedClass.getName().replace('.', '$');
  }

  private static boolean isMatchingField(String name, Field field) {
    return field.getName().equals(name) && !isStatic(field.getModifiers());
  }

  private static boolean isCandidateMethod(String name, Method method) {
    return method.getName().equals(name) && isPublic(method.getModifiers()) && !isAbstract(method.getModifiers());
  }
}
