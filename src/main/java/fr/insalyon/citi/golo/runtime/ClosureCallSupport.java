package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.*;
import java.util.Arrays;

import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodType.methodType;

public class ClosureCallSupport {

  static class InlineCache extends MutableCallSite {

    MethodHandle fallback;

    public InlineCache(MethodType type) {
      super(type);
    }
  }

  private static final MethodHandle GUARD;
  private static final MethodHandle FALLBACK;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      GUARD = lookup.findStatic(
          ClosureCallSupport.class,
          "guard",
          methodType(boolean.class, MethodHandle.class, MethodHandle.class));

      FALLBACK = lookup.findStatic(
          ClosureCallSupport.class,
          "fallback",
          methodType(Object.class, InlineCache.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) {
    InlineCache callSite = new InlineCache(type);
    MethodHandle fallbackHandle = FALLBACK
        .bindTo(callSite)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    callSite.fallback = fallbackHandle;
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  public static boolean guard(MethodHandle expected, MethodHandle actual) {
    return expected == actual;
  }

  public static Object fallback(InlineCache callSite, Object[] args) throws Throwable {
    MethodHandle target = (MethodHandle) args[0];
    MethodHandle invoker = MethodHandles.dropArguments(target, 0, MethodHandle.class);
    MethodHandle guard = GUARD.bindTo(target);
    MethodHandle root = guardWithTest(guard, invoker, callSite.fallback);
    callSite.setTarget(root);
    return invoker.invokeWithArguments(args);
  }
}
