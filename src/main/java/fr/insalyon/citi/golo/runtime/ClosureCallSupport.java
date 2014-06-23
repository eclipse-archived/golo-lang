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

import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodType.methodType;
import static fr.insalyon.citi.golo.runtime.TypeMatching.isLastArgumentAnArray;

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
    MethodType type = invoker.type();
    if (type.parameterType(type.parameterCount() - 1) == Object[].class) {
      if (target.isVarargsCollector() && isLastArgumentAnArray(type.parameterCount(), args)) {
        invoker = invoker.asFixedArity().asType(callSite.type());
      } else {
        invoker = invoker.asCollector(Object[].class, callSite.type().parameterCount() - target.type().parameterCount());
      }
    }
    MethodHandle guard = GUARD.bindTo(target);
    MethodHandle root = guardWithTest(guard, invoker, callSite.fallback);
    callSite.setTarget(root);
    return invoker.invokeWithArguments(args);
  }
}
