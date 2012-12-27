package fr.insalyon.citi.golo.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class TypeMatching {

  private static final Map<Class, Class> PRIMITIVE_MAP = new HashMap<Class, Class>() {
    {
      put(byte.class, Byte.class);
      put(short.class, Short.class);
      put(char.class, Character.class);
      put(int.class, Integer.class);
      put(long.class, Long.class);
      put(float.class, Float.class);
      put(double.class, Double.class);
    }
  };

  static boolean haveEnoughArgumentsForVarargs(Object[] arguments, Constructor constructor, Class<?>[] parameterTypes) {
    return constructor.isVarArgs() && (arguments.length >= parameterTypes.length);
  }

  static boolean haveEnoughArgumentsForVarargs(Object[] arguments, Method method, Class<?>[] parameterTypes) {
    return method.isVarArgs() && (arguments.length >= parameterTypes.length);
  }

  static boolean haveSameNumberOfArguments(Object[] arguments, Class<?>[] parameterTypes) {
    return parameterTypes.length == arguments.length;
  }

  static boolean canAssign(Class<?>[] types, Object[] arguments, boolean varArgs) {
    if (types.length == 0) {
      return true;
    }
    for (int i = 0; i < types.length - 1; i++) {
      if (!valueAndTypeMatch(types[i], arguments[i])) {
        return false;
      }
    }
    final int last = types.length - 1;
    if (varArgs) {
      return valueAndTypeMatch(types[last].getComponentType(), arguments[last]);
    }
    return valueAndTypeMatch(types[last], arguments[last]);
  }

  private static boolean valueAndTypeMatch(Class<?> type, Object value) {
    return primitiveCompatible(type, value) || (type.isInstance(value) || value == null);
  }

  private static boolean primitiveCompatible(Class<?> type, Object value) {
    if (!type.isPrimitive() || value == null) {
      return false;
    }
    return PRIMITIVE_MAP.get(type) == value.getClass();
  }
}
