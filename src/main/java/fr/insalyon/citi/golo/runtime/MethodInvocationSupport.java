package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodType.methodType;
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

    Object searchResult = findMethodOrField(receiverClass, inlineCache.name, type.parameterArray());
    if (searchResult == null) {
      throw new NoSuchMethodError(receiverClass + "::" + inlineCache.name);
    }
    if (searchResult.getClass() == Method.class) {
      target = inlineCache.callerLookup.unreflect((Method) searchResult).asType(type);
    } else {
      target = inlineCache.callerLookup.unreflectGetter((Field) searchResult).asType(type);
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

  private static Object findMethodOrField(Class<?> receiverClass, String name, Class<?>[] argumentTypes) {
    for (Method method : receiverClass.getMethods()) {
      if (method.getName().equals(name) && (isPublic(method.getModifiers()))) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == (argumentTypes.length - 1)) {
          return method;
        } else if (method.isVarArgs() && (argumentTypes.length > parameterTypes.length)) {
          return method;
        }
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
