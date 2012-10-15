package fr.insalyon.citi.golo.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.isStatic;

class BootstrapHelpers {

  static boolean containsPrimitiveTypes(Class<?>[] types) {
    for (Class<?> type : types) {
      if (isPrimitive(type)) {
        return true;
      }
    }
    return false;
  }

  static boolean isPrimitive(Class<?> type) {
    if (type.isPrimitive()) {
      return true;
    } else if (type.isArray()) {
      return isPrimitive(type.getComponentType());
    }
    return false;
  }

  static Object findStaticMethodOrField(Class<?> klass, String name, Class<?>[] argumentTypes) {
    for (Method method : klass.getDeclaredMethods()) {
      if (method.getName().equals(name)) {
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
}
