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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static fr.insalyon.citi.golo.runtime.TypeMatching.*;
import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodType.methodType;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;

public final class FunctionCallSupport {

  static class FunctionCallSite extends MutableCallSite {

    final Lookup callerLookup;
    final String name;

    FunctionCallSite(MethodHandles.Lookup callerLookup, String name, MethodType type) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
    }
  }

  private static final MethodHandle FALLBACK;
  private static final MethodHandle SAM_FILTER;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      FALLBACK = lookup.findStatic(
          FunctionCallSupport.class,
          "fallback",
          methodType(Object.class, FunctionCallSite.class, Object[].class));
      SAM_FILTER = lookup.findStatic(
          FunctionCallSupport.class,
          "samFilter",
          methodType(Object.class, Class.class, Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static Object samFilter(Class<?> type, Object value) {
    if (value instanceof MethodHandle) {
      return MethodHandleProxies.asInterfaceInstance(type, (MethodHandle) value);
    }
    return value;
  }

  public static CallSite bootstrap(Lookup caller, String name, MethodType type) throws IllegalAccessException, ClassNotFoundException {
    FunctionCallSite callSite = new FunctionCallSite(caller, name.replaceAll("#", "\\."), type);
    MethodHandle fallbackHandle = FALLBACK
        .bindTo(callSite)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  public static Object fallback(FunctionCallSite callSite, Object[] args) throws Throwable {
    String functionName = callSite.name;
    MethodType type = callSite.type();
    Lookup caller = callSite.callerLookup;
    Class<?> callerClass = caller.lookupClass();

    MethodHandle handle = null;
    Object result = findStaticMethodOrField(callerClass, functionName, args);
    if (result == null) {
      result = findClassWithStaticMethodOrField(callerClass, functionName, args);
    }
    if (result == null) {
      result = findClassWithStaticMethodOrFieldFromImports(callerClass, functionName, args);
    }
    if (result == null) {
      result = findClassWithConstructor(callerClass, functionName, args);
    }
    if (result == null) {
      result = findClassWithConstructorFromImports(callerClass, functionName, args);
    }
    if (result == null) {
      throw new NoSuchMethodError(functionName);
    }

    Class[] types = null;
    if (result instanceof Method) {
      Method method = (Method) result;
      checkLocalFunctionCallFromSameModuleAugmentation(method, callerClass.getName());
      types = method.getParameterTypes();
      if (method.isVarArgs() && isLastArgumentAnArray(types.length, args)) {
        handle = caller.unreflect(method).asFixedArity().asType(type);
      } else {
        handle = caller.unreflect(method).asType(type);
      }
    } else if (result instanceof Constructor) {
      Constructor constructor = (Constructor) result;
      types = constructor.getParameterTypes();
      if (constructor.isVarArgs() && isLastArgumentAnArray(types.length, args)) {
        handle = caller.unreflectConstructor(constructor).asFixedArity().asType(type);
      } else {
        handle = caller.unreflectConstructor(constructor).asType(type);
      }
    } else {
      Field field = (Field) result;
      handle = caller.unreflectGetter(field).asType(type);
    }
    handle = insertSAMFilter(handle, types, 0);

    callSite.setTarget(handle);
    return handle.invokeWithArguments(args);
  }

  public static MethodHandle insertSAMFilter(MethodHandle handle, Class[] types, int startIndex) {
    if (types != null) {
      for (int i = 0; i < types.length; i++) {
        if (isSAM(types[i])) {
          handle = MethodHandles.filterArguments(handle, startIndex + i, SAM_FILTER.bindTo(types[i]));
        }
      }
    }
    return handle;
  }

  private static void checkLocalFunctionCallFromSameModuleAugmentation(Method method, String callerClassName) {
    if (isPrivate(method.getModifiers()) && callerClassName.contains("$")) {
      String prefix = callerClassName.substring(0, callerClassName.indexOf("$"));
      if (method.getDeclaringClass().getName().equals(prefix)) {
        method.setAccessible(true);
      }
    }
  }

  private static Object findClassWithConstructorFromImports(Class<?> callerClass, String classname, Object[] args) {
    String[] imports = Module.imports(callerClass);
    for (String imported : imports) {
      Object result = findClassWithConstructor(callerClass, imported + "." + classname, args);
      if (result != null) {
        return result;
      }
      if (imported.endsWith(classname)) {
        result = findClassWithConstructor(callerClass, imported, args);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  private static Object findClassWithConstructor(Class<?> callerClass, String classname, Object[] args) {
    try {
      Class<?> targetClass = Class.forName(classname, true, callerClass.getClassLoader());
      for (Constructor<?> constructor : targetClass.getConstructors()) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (haveSameNumberOfArguments(args, parameterTypes) || haveEnoughArgumentsForVarargs(args, constructor, parameterTypes)) {
          if (canAssign(parameterTypes, args, constructor.isVarArgs())) {
            return constructor;
          }
        }
      }
    } catch (ClassNotFoundException ignored) {
    }
    return null;
  }

  private static Object findClassWithStaticMethodOrFieldFromImports(Class<?> callerClass, String functionName, Object[] args) {
    String[] imports = Module.imports(callerClass);
    String[] classAndMethod = null;
    final int classAndMethodSeparator = functionName.lastIndexOf(".");
    if (classAndMethodSeparator > 0) {
      classAndMethod = new String[]{
          functionName.substring(0, classAndMethodSeparator),
          functionName.substring(classAndMethodSeparator + 1)
      };
    }
    for (String importClassName : imports) {
      try {
        Class<?> importClass;
        try {
          importClass = Class.forName(importClassName, true, callerClass.getClassLoader());
        } catch (ClassNotFoundException expected) {
          if (classAndMethod == null) {
            throw expected;
          }
          importClass = Class.forName(importClassName + "." + classAndMethod[0], true, callerClass.getClassLoader());
        }
        String lookup = (classAndMethod == null) ? functionName : classAndMethod[1];
        Object result = findStaticMethodOrField(importClass, lookup, args);
        if (result != null) {
          return result;
        }
      } catch (ClassNotFoundException ignored) {
      }
    }
    return null;
  }

  private static Object findClassWithStaticMethodOrField(Class<?> callerClass, String functionName, Object[] args) {
    int methodClassSeparatorIndex = functionName.lastIndexOf(".");
    if (methodClassSeparatorIndex >= 0) {
      String className = functionName.substring(0, methodClassSeparatorIndex);
      String methodName = functionName.substring(methodClassSeparatorIndex + 1);
      try {
        Class<?> targetClass = Class.forName(className, true, callerClass.getClassLoader());
        return findStaticMethodOrField(targetClass, methodName, args);
      } catch (ClassNotFoundException ignored) {
      }
    }
    return null;
  }

  private static Object findStaticMethodOrField(Class<?> klass, String name, Object[] arguments) {
    for (Method method : klass.getDeclaredMethods()) {
      if (methodMatches(name, arguments, method)) {
        return method;
      }
    }
    for (Method method : klass.getMethods()) {
      if (methodMatches(name, arguments, method)) {
        return method;
      }
    }
    if (arguments.length == 0) {
      for (Field field : klass.getDeclaredFields()) {
        if (fieldMatches(name, field)) {
          return field;
        }
      }
      for (Field field : klass.getFields()) {
        if (fieldMatches(name, field)) {
          return field;
        }
      }
    }
    return null;
  }

  private static boolean methodMatches(String name, Object[] arguments, Method method) {
    if (method.getName().equals(name) && isStatic(method.getModifiers())) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (haveSameNumberOfArguments(arguments, parameterTypes) || haveEnoughArgumentsForVarargs(arguments, method, parameterTypes)) {
        if (canAssign(parameterTypes, arguments, method.isVarArgs())) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean fieldMatches(String name, Field field) {
    return field.getName().equals(name) && isStatic(field.getModifiers());
  }
}
