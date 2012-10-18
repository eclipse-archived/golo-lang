package fr.insalyon.citi.golo.runtime;

class BootstrapHelpers {

  static boolean havePrimitiveArray(Class<?>[] types) {
    for (Class<?> type : types) {
      if (type.isArray() && type.getComponentType().isPrimitive()) {
        return true;
      }
    }
    return false;
  }

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
