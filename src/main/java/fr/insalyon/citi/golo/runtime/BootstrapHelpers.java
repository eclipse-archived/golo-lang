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
}
