package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.Lookup;
import static java.lang.reflect.Modifier.isStatic;

public final class FunctionCallSupport {

  public static CallSite bootstrap(Lookup caller, String name, MethodType type) throws IllegalAccessException, ClassNotFoundException {
    String functionName = name.replaceAll("#", "\\.");
    Class<?> callerClass = caller.lookupClass();
    MethodHandle handle = null;
    Object result = findStaticMethodOrField(callerClass, functionName, type.parameterArray());
    if (result == null) {
      result = findClassWithStaticMethodOrField(callerClass, functionName, type);
    }
    if (result == null) {
      result = findClassWithStaticMethodOrFieldFromImports(callerClass, functionName, type);
    }
    if (result == null) {
      result = findClassWithConstructor(callerClass, functionName, type);
    }
    if (result == null) {
      result = findClassWithConstructorFromImports(callerClass, functionName, type);
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
    return new ConstantCallSite(handle);
  }

  private static Object findClassWithConstructorFromImports(Class<?> callerClass, String classname, MethodType type) {
    String[] imports = imports(callerClass);
    for (String imported : imports) {
      Object result = findClassWithConstructor(callerClass, imported + "." + classname, type);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static Object findClassWithConstructor(Class<?> callerClass, String classname, MethodType type) {
    try {
      Class<?> targetClass = Class.forName(classname, true, callerClass.getClassLoader());
      for (Constructor<?> constructor : targetClass.getConstructors()) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (containsPrimitiveTypes(parameterTypes)) {
          continue;
        }
        int requiredParameterCount = type.parameterCount();
        if (parameterTypes.length == requiredParameterCount) {
          return constructor;
        } else if (constructor.isVarArgs() && (requiredParameterCount >= parameterTypes.length)) {
          return constructor;
        }
      }
    } catch (ClassNotFoundException ignored) {
    }
    return null;
  }

  private static Object findClassWithStaticMethodOrFieldFromImports(Class<?> callerClass, String functionName, MethodType type) {
    String[] imports = imports(callerClass);
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
        Object result = findStaticMethodOrField(importClass, lookup, type.parameterArray());
        if (result != null) {
          return result;
        }
      } catch (ClassNotFoundException ignored) {
      }
    }
    return null;
  }

  private static String[] imports(Class<?> callerClass) {
    String[] imports;
    try {
      Method $imports = callerClass.getMethod("$imports");
      imports = (String[]) $imports.invoke(null);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      // This can only happen as part of the unit tests, because the lookup does not originate from
      // a Golo module class, hence it doesn't have a $imports() static method.
      imports = new String[]{};
    }
    return imports;
  }

  private static Object findClassWithStaticMethodOrField(Class<?> callerClass, String functionName, MethodType type) {
    int methodClassSeparatorIndex = functionName.lastIndexOf(".");
    if (methodClassSeparatorIndex >= 0) {
      String className = functionName.substring(0, methodClassSeparatorIndex);
      String methodName = functionName.substring(methodClassSeparatorIndex + 1);
      try {
        Class<?> targetClass = Class.forName(className, true, callerClass.getClassLoader());
        return findStaticMethodOrField(targetClass, methodName, type.parameterArray());
      } catch (ClassNotFoundException ignored) {
      }
    }
    return null;
  }

  private static Object findStaticMethodOrField(Class<?> klass, String name, Class<?>[] argumentTypes) {
    for (Method method : klass.getDeclaredMethods()) {
      if (method.getName().equals(name) && isStatic(method.getModifiers())) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == argumentTypes.length) {
          return method;
        } else if (method.isVarArgs() && (argumentTypes.length >= parameterTypes.length)) {
          return method;
        }
      }
    }
    if (argumentTypes.length == 0) {
      for (Field field : klass.getDeclaredFields()) {
        if (field.getName().equals(name) && isStatic(field.getModifiers())) {
          return field;
        }
      }
    }
    return null;
  }

  private static boolean containsPrimitiveTypes(Class<?>[] types) {
    for (Class<?> type : types) {
      if (isPrimitive(type)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isPrimitive(Class<?> type) {
    if (type.isPrimitive()) {
      return true;
    } else if (type.isArray()) {
      return isPrimitive(type.getComponentType());
    }
    return false;
  }
}
