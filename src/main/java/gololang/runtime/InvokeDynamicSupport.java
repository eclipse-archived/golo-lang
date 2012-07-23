package gololang.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.Lookup;

public final class InvokeDynamicSupport {

  public static CallSite bootstrapFunctionInvocation(Lookup caller, String name, MethodType type) throws IllegalAccessException, ClassNotFoundException {
    Class<?> callerClass = caller.lookupClass();
    MethodHandle handle = null;
    Method method = findStaticMethod(callerClass, name, type.parameterArray());
    if (method != null) {
      handle = caller.unreflect(method).asType(type);
    } else {
      int methodClassSeparatorIndex = name.lastIndexOf(".");
      if (methodClassSeparatorIndex == -1) {
        throw new NoSuchMethodError(name);
      }
      String className = name.substring(0, methodClassSeparatorIndex);
      String methodName = name.substring(methodClassSeparatorIndex + 1);
      Class<?> targetClass = Class.forName(className, true, callerClass.getClassLoader());
      method = findStaticMethod(targetClass, methodName, type.parameterArray());
      if (method == null) {
        throw new NoSuchMethodError(name);
      }
      handle = caller.unreflect(method).asType(type);
    }
    return new ConstantCallSite(handle);
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
