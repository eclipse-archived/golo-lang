package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodType.methodType;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;

public class MethodInvocationSupport {

  /*
   * This code is heavily inspired from the inline cache construction from
   * Remi Forax's JSR292 cookbooks.
   */

  static class InlineCache extends MutableCallSite {

    final MethodHandles.Lookup callerLookup;
    final String name;
    int depth = 0;

    InlineCache(MethodHandles.Lookup callerLookup, String name, MethodType type) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
    }

    boolean isMegaMorphic() {
      return depth >= 5;
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
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  public static boolean guard(Class<?> expected, Object receiver) {
    return receiver.getClass() == expected;
  }

  public static Object fallback(InlineCache inlineCache, Object[] args) throws Throwable {

    MethodHandle target;
    MethodType type = inlineCache.type();
    Class<?> receiverClass = args[0].getClass();
    boolean makeAccessible = !isPublic(receiverClass.getModifiers());

    Object searchResult = findMethodOrField(receiverClass, inlineCache.name, type.parameterArray(), args);
    if (searchResult == null) {
      throw new NoSuchMethodError(receiverClass + "::" + inlineCache.name);
    }
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
      target = inlineCache.callerLookup.unreflectGetter(field).asType(type);
    }

    if (inlineCache.isMegaMorphic()) {
      return target.invokeWithArguments(args);
    }

    MethodHandle guard = GUARD.bindTo(receiverClass);
    MethodHandle root = guardWithTest(guard, target, inlineCache.getTarget());
    inlineCache.setTarget(root);
    inlineCache.depth = inlineCache.depth + 1;
    return target.invokeWithArguments(args);
  }

  private static int argumentsScore(Class<?>[] types, Object[] args) {
    int score = 0;
    if (args.length == types.length + 1) {
      score = 10;
    }
    for (int i = 0; i < types.length; i++) {
      Class<?> type = types[i];
      if (type == Object.class) {
        score = score + 10;
      } else if (type.isArray() && type.getComponentType().isPrimitive()) {
        score = score - 10;
      }
    }

    return score;
  }

  private static Object findMethodOrField(Class<?> receiverClass, String name, Class<?>[] argumentTypes, Object[] args) {

    List<Method> candidates = new LinkedList<>();
    for (Method method : receiverClass.getMethods()) {
      if (method.getName().equals(name) && isPublic(method.getModifiers()) && !isAbstract(method.getModifiers())) {
        candidates.add(method);
      }
    }

    if (candidates.size() == 1) {
      return candidates.get(0);
    }

    if (!candidates.isEmpty()) {
      Method chosen = null;
      int bestScore = 0;
      for (Method method : candidates) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if ((parameterTypes.length == (argumentTypes.length - 1)) || (parameterTypes.length == (argumentTypes.length - 1))) {
          int score = argumentsScore(parameterTypes, args);
          if (score > bestScore) {
            chosen = method;
            bestScore = score;
          }
        }
      }
      if (chosen != null) {
        return chosen;
      }
    }

    if (argumentTypes.length == 1) {
      for (Field field : receiverClass.getDeclaredFields()) {
        if (field.getName().equals(name)) {
          return field;
        }
      }
    }
    return null;
  }
}
