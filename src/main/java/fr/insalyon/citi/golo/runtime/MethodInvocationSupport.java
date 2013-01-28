package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

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

  static class PolymorphicInlineCache extends MutableCallSite {

    final MethodHandles.Lookup callerLookup;
    final String name;
    int depth = 0;

    PolymorphicInlineCache(MethodHandles.Lookup callerLookup, String name, MethodType type) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
    }

    boolean isMegaMorphic() {
      return false;
      // TODO: check megamorphic fallback strategies
      // return depth >= 5;
    }
  }

  private static final MethodHandle GUARD;
  private static final MethodHandle FALLBACK;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      GUARD = lookup.findStatic(
          MethodInvocationSupport.class,
          "guard",
          methodType(boolean.class, Class.class, Object.class));

      FALLBACK = lookup.findStatic(
          MethodInvocationSupport.class,
          "fallback",
          methodType(Object.class, PolymorphicInlineCache.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) {
    PolymorphicInlineCache callSite = new PolymorphicInlineCache(caller, name, type);
    MethodHandle fallbackHandle = FALLBACK
        .bindTo(callSite)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  public static boolean guard(Class<?> expected, Object receiver) {
    return receiver.getClass() == expected;
  }

  public static Object fallback(PolymorphicInlineCache inlineCache, Object[] args) throws Throwable {

    Class<?> receiverClass = args[0].getClass();
    MethodHandle target = findTarget(receiverClass, inlineCache, args);

    if (inlineCache.isMegaMorphic()) {
      return target.invokeWithArguments(args);
    }

    MethodHandle guard = GUARD.bindTo(receiverClass);
    MethodHandle root = guardWithTest(guard, target, inlineCache.getTarget());
    inlineCache.setTarget(root);
    inlineCache.depth = inlineCache.depth + 1;
    return target.invokeWithArguments(args);
  }

  private static MethodHandle findTarget(Class<?> receiverClass, PolymorphicInlineCache inlineCache, Object[] args) throws IllegalAccessException {
    MethodHandle target;
    MethodType type = inlineCache.type();
    boolean makeAccessible = !isPublic(receiverClass.getModifiers());

    Object searchResult = findMethodOrField(receiverClass, inlineCache.name, type.parameterArray(), args);
    if (searchResult != null) {
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

  private static MethodHandle findInPimps(Class<?> receiverClass, PolymorphicInlineCache inlineCache) {
    Class<?> callerClass = inlineCache.callerLookup.lookupClass();
    String name = inlineCache.name;
    MethodType type = inlineCache.type();
    Lookup lookup = inlineCache.callerLookup;

    ClassLoader classLoader = callerClass.getClassLoader();
    for (String pimp : Module.pimps(callerClass)) {
      try {
        Class<?> pimpedClass = classLoader.loadClass(pimp);
        if (pimpedClass.isAssignableFrom(receiverClass)) {
          Class<?> pimpClass = classLoader.loadClass(pimpClassName(callerClass, pimpedClass));
          for (Method method : pimpClass.getMethods()) {
            if (isCandidateMethod(name, method)) {
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
                if (isCandidateMethod(name, method)) {
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
