package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodType.methodType;
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

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      FALLBACK = lookup.findStatic(
          FunctionCallSupport.class,
          "fallback",
          methodType(Object.class, FunctionCallSite.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
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
    if (result instanceof Method) {
      Method method = (Method) result;
      handle = caller.unreflect(method).asType(type);
    } else if (result instanceof Constructor) {
      Constructor constructor = (Constructor) result;
      handle = caller.unreflectConstructor(constructor).asType(type);
    } else if (result instanceof Field) {
      Field field = (Field) result;
      handle = caller.unreflectGetter(field).asType(type);
    }

    callSite.setTarget(handle);
    return handle.invokeWithArguments(args);
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
        if (TypeMatching.haveSameNumberOfArguments(args, parameterTypes) || TypeMatching.haveEnoughArgumentsForVarargs(args, constructor, parameterTypes)) {
          if (TypeMatching.canAssign(parameterTypes, args, constructor.isVarArgs())) {
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
        Class<?> importClass = Class.forName(importClassName, true, callerClass.getClassLoader());
        String lookup = functionName;
        if ((classAndMethod != null) && (importClassName.endsWith(classAndMethod[0]))) {
          lookup = classAndMethod[1];
        }
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
      if (method.getName().equals(name) && isStatic(method.getModifiers())) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (TypeMatching.haveSameNumberOfArguments(arguments, parameterTypes) || TypeMatching.haveEnoughArgumentsForVarargs(arguments, method, parameterTypes)) {
          if (TypeMatching.canAssign(parameterTypes, arguments, method.isVarArgs())) {
            return method;
          }
        }
      }
    }
    if (arguments.length == 0) {
      for (Field field : klass.getDeclaredFields()) {
        if (field.getName().equals(name) && isStatic(field.getModifiers())) {
          return field;
        }
      }
    }
    return null;
  }
}
