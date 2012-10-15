package fr.insalyon.citi.golo.runtime;

class BootstrapHelpers {

  public static boolean containsPrimitiveTypes(Class<?>[] types) {
    for (Class<?> type : types) {
      if (isPrimitive(type)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPrimitive(Class<?> type) {
    if (type.isPrimitive()) {
      return true;
    } else if (type.isArray()) {
      return isPrimitive(type.getComponentType());
    }
    return false;
  }
}
