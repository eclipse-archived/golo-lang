package gololang.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.Lookup;

public final class InvokeDynamicFunctionCallSupport {

  public static CallSite bootstrap(Lookup caller, String name, MethodType type) throws IllegalAccessException, ClassNotFoundException {
    String functionName = name.replaceAll("#", "\\.");
    Class<?> callerClass = caller.lookupClass();
    MethodHandle handle;
    Method method = findStaticMethod(callerClass, functionName, type.parameterArray());
    if (method == null) {
      method = findClassWithStaticMethod(callerClass, functionName, type);
    }
    if (method == null) {
      method = findClassWithStaticMethodFromImports(callerClass, functionName, type);
    }
    if (method == null) {
      throw new NoSuchMethodError(functionName);
    }
    handle = caller.unreflect(method).asType(type);
    return new ConstantCallSite(handle);
  }

  private static Method findClassWithStaticMethodFromImports(Class<?> callerClass, String functionName, MethodType type) throws IllegalAccessException, ClassNotFoundException {
    Method $imports;
    String[] imports;
    try {
      $imports = callerClass.getMethod("$imports");
      imports = (String[]) $imports.invoke(null);
    } catch (NoSuchMethodException | InvocationTargetException e) {
      // This can only happen as part of the unit tests, because the lookup does not originate from
      // a Golo module class, hence it doesn't have a $imports() static method.
      imports = new String[] { };
    }
    for (String importClassName : imports) {
      Class<?> importClass = Class.forName(importClassName, true, callerClass.getClassLoader());
      Method method = findStaticMethod(importClass, functionName, type.parameterArray());
      if (method != null) {
        return method;
      }
    }
    return null;
  }

  private static Method findClassWithStaticMethod(Class<?> callerClass, String functionName, MethodType type) throws ClassNotFoundException {
    int methodClassSeparatorIndex = functionName.lastIndexOf(".");
    if (methodClassSeparatorIndex >= 0) {
      String className = functionName.substring(0, methodClassSeparatorIndex);
      String methodName = functionName.substring(methodClassSeparatorIndex + 1);
      Class<?> targetClass = Class.forName(className, true, callerClass.getClassLoader());
      return findStaticMethod(targetClass, methodName, type.parameterArray());
    }
    return null;
  }

  private static Method findStaticMethod(Class<?> klass, String name, Class<?>[] argumentTypes) {
    for (Method method : klass.getDeclaredMethods()) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (method.getName().equals(name) && parameterTypes.length == argumentTypes.length) {
        return method;
      }
    }
    return null;
  }
}
