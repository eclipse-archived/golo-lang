package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.*;

import static java.lang.invoke.MethodType.methodType;

public class OperatorSupport {

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type, int arity) throws NoSuchMethodException, IllegalAccessException {
    MethodHandle handle;
    if (arity == 1) {
      handle = caller.findStatic(OperatorSupport.class, name, methodType(Object.class, Object.class));
    } else {
      handle = caller.findStatic(OperatorSupport.class, name, methodType(Object.class, Object.class, Object.class));
    }
    return new ConstantCallSite(handle);
  }

  private static boolean bothNotNull(Object a, Object b) {
    return (a != null) && (b != null);
  }

  private static boolean isNotNullAndString(Object obj) {
    return (obj != null) && (obj.getClass() == String.class);
  }

  private static boolean isString(Object obj) {
    return obj.getClass() == String.class;
  }

  private static boolean isInteger(Object obj) {
    return obj.getClass() == Integer.class;
  }

  private static boolean isLong(Object obj) {
    return obj.getClass() == Long.class;
  }

  private static boolean isComparable(Object obj) {
    return obj instanceof Comparable<?>;
  }

  private static boolean isBoolean(Object obj) {
    return obj.getClass() == Boolean.class;
  }

  public static Object plus(Object a, Object b) {
    if (bothNotNull(a, b)) {
      if (isInteger(a) && isInteger(b)) {
        return (Integer) a + (Integer) b;
      }
      if (isLong(a) && isLong(b)) {
        return (Long) a + (Long) b;
      }
      if (isLong(a) && isInteger(b)) {
        return (Long) a + (long) ((Integer) b);
      }
      if (isInteger(a) && isLong(b)) {
        return (long) ((Integer) a) + (Long) b;
      }
    }
    if (isNotNullAndString(a) || isNotNullAndString(b)) {
      return new StringBuilder().append(a).append(b).toString();
    }
    return reject(a, b, "+");
  }

  public static Object minus(Object a, Object b) {
    if (bothNotNull(a, b)) {
      if (isInteger(a) && isInteger(b)) {
        return (Integer) a - (Integer) b;
      }
      if (isLong(a) && isLong(b)) {
        return (Long) a - (Long) b;
      }
      if (isLong(a) && isInteger(b)) {
        return (Long) a - (long) ((Integer) b);
      }
      if (isInteger(a) && isLong(b)) {
        return (long) ((Integer) a) - (Long) b;
      }
    }
    return reject(a, b, "-");
  }

  public static Object times(Object a, Object b) {
    if (bothNotNull(a, b)) {
      if (isInteger(a) && isInteger(b)) {
        return (Integer) a * (Integer) b;
      }
      if (isLong(a) && isLong(b)) {
        return (Long) a * (Long) b;
      }
      if (isLong(a) && isInteger(b)) {
        return (Long) a * (long) ((Integer) b);
      }
      if (isInteger(a) && isLong(b)) {
        return (long) ((Integer) a) * (Long) b;
      }
      if (isInteger(a) && isString(b)) {
        return repeat((String) b, (Integer) a);
      }
      if (isString(a) && isInteger(b)) {
        return repeat((String) a, (Integer) b);
      }
    }
    return reject(a, b, "*");
  }

  private static String repeat(String string, int n) {
    StringBuilder builder = new StringBuilder(string);
    for (int i = 1; i < n; i++) {
      builder.append(string);
    }
    return builder.toString();
  }

  public static Object divide(Object a, Object b) {
    if (bothNotNull(a, b)) {
      if (isInteger(a) && isInteger(b)) {
        return (Integer) a / (Integer) b;
      }
      if (isLong(a) && isLong(b)) {
        return (Long) a / (Long) b;
      }
      if (isLong(a) && isInteger(b)) {
        return (Long) a / (long) ((Integer) b);
      }
      if (isInteger(a) && isLong(b)) {
        return (long) ((Integer) a) / (Long) b;
      }
    }
    return reject(a, b, "/");
  }

  public static Object equals(Object a, Object b) {
    return (a == b) || ((a != null) && a.equals(b));
  }

  public static Object notequals(Object a, Object b) {
    return (a != b) && (((a != null) && !a.equals(b)) || ((b != null) && !b.equals(a)));
  }

  public static Object less(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) < 0;
    }
    return reject(a, b, "<");
  }

  public static Object lessorequals(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) <= 0;
    }
    return reject(a, b, "<=");
  }

  public static Object more(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) > 0;
    }
    return reject(a, b, ">");
  }

  public static Object moreorequals(Object a, Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) >= 0;
    }
    return reject(a, b, ">=");
  }

  public static Object and(Object a, Object b) {
    if (bothNotNull(a, b) && isBoolean(a) && isBoolean(b)) {
      return ((Boolean) a) && ((Boolean) b);
    }
    return reject(a, b, "and");
  }

  public static Object or(Object a, Object b) {
    if (bothNotNull(a, b) && isBoolean(a) && isBoolean(b)) {
      return ((Boolean) a) || ((Boolean) b);
    }
    return reject(a, b, "or");
  }

  public static Object not(Object a) {
    if (a != null && isBoolean(a)) {
      return !((Boolean) a);
    }
    return reject(a, "not");
  }

  private static Object reject(Object a, String symbol) throws IllegalArgumentException {
    throw new IllegalArgumentException(String.format("Operator %s is not supported for type %s", symbol, a.getClass()));
  }

  private static Object reject(Object a, Object b, String symbol) throws IllegalArgumentException {
    throw new IllegalArgumentException(String.format("Operator %s is not supported for types %s and %s", symbol, a.getClass(), b.getClass()));
  }

  public static Object is(Object a, Object b) {
    return a == b;
  }

  public static Object isnt(Object a, Object b) {
    return a != b;
  }
}
