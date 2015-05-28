/*
 * Copyright 2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.insalyon.citi.golo.runtime;

import gololang.annotations.DecoratedBy;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import static java.lang.invoke.MethodType.methodType;
import java.lang.reflect.Method;

public final class DecoratorsHelper {

  private static final MethodHandle FUNCTION_REFERENCE_TO_METHODHANDLE;
  private static final MethodHandle INVOKE_WITH_ARGUMENTS;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      FUNCTION_REFERENCE_TO_METHODHANDLE = lookup.findStatic(
          DecoratorsHelper.class,
          "functionReferenceToMethodHandle",
          MethodType.methodType(Object.class, Object.class));
      INVOKE_WITH_ARGUMENTS = lookup.findVirtual(
          MethodHandle.class,
          "invokeWithArguments",
          MethodType.methodType(Object.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  private DecoratorsHelper() {
  }

  public static boolean isMethodDecorated(Method method) {
    return method.isAnnotationPresent(DecoratedBy.class);
  }

  public static Method getDecoratorMethod(Method decorated) {
    try {
      return decorated.getDeclaringClass().getDeclaredMethod(decorated.getAnnotation(DecoratedBy.class).value(), Object.class);
    } catch (NoSuchMethodException | SecurityException ex) {
      throw new IllegalStateException("Unable to get the decorator for a method marked as decorated", ex);
    }
  }

  private static Object functionReferenceToMethodHandle(Object retValue) {
    return ((gololang.FunctionReference) retValue).handle();
  }

  public static MethodHandle getDecoratedMethodHandle(MethodHandles.Lookup caller, Method originalMethod, int arity) {
    try {
      Method decoratorMethod = getDecoratorMethod(originalMethod);
      MethodHandle decorator = caller.unreflect(decoratorMethod);
      decorator = MethodHandles.filterReturnValue(decorator, FUNCTION_REFERENCE_TO_METHODHANDLE);
      MethodHandle original = caller.unreflect(originalMethod);
      decorator = decorator.bindTo(new gololang.FunctionReference(original)).asType(methodType(MethodHandle.class));
      if (arity < 0) {
        MethodHandle combined = MethodHandles.foldArguments(INVOKE_WITH_ARGUMENTS, decorator);
        return combined.asVarargsCollector(Object[].class);
      } else {
        MethodHandle invoker = MethodHandles.invoker(MethodType.genericMethodType(arity));
        MethodHandle combined = MethodHandles.foldArguments(invoker, decorator);
        return combined;
      }
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException("Unable to get the decorator for a method marked as decorated", ex);
    }
  }

  public static MethodHandle getDecoratedMethodHandle(Method originalMethod, int arity) {
    return getDecoratedMethodHandle(MethodHandles.lookup(), originalMethod, arity);
  }

  public static MethodHandle getDecoratedMethodHandle(Method originalMethod) {
    return getDecoratedMethodHandle(MethodHandles.lookup(), originalMethod, -1);
  }
}
