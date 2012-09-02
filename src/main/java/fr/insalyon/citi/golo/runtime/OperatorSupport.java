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

  public static Object plus(Object a, Object b) {
    if ((a instanceof Integer) && (b instanceof Integer)) {
      return (Integer) a + (Integer) b;
    }
    if ((a instanceof String) || (b instanceof String)) {
      return new StringBuilder().append(a).append(b).toString();
    }
    return reject(a, b, "+");
  }

  public static Object minus(Object a, Object b) {
    if ((a instanceof Integer) && (b instanceof Integer)) {
      return (Integer) a - (Integer) b;
    }
    return reject(a, b, "-");
  }

  public static Object times(Object a, Object b) {
    if ((a instanceof Integer) && (b instanceof Integer)) {
      return (Integer) a * (Integer) b;
    }
    if ((a instanceof Integer) && (b instanceof String)) {
      return repeat((String) b, (Integer) a);
    }
    if ((a instanceof String) && (b instanceof Integer)) {
      return repeat((String) a, (Integer) b);
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
    if ((a instanceof Integer) && (b instanceof Integer)) {
      return (Integer) a / (Integer) b;
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
    if ((a instanceof Comparable) && (b instanceof Comparable)) {
      return ((Comparable) a).compareTo(b) < 0;
    }
    return reject(a, b, "<");
  }

  public static Object lessorequals(Object a, Object b) {
    if ((a instanceof Comparable) && (b instanceof Comparable)) {
      return ((Comparable) a).compareTo(b) <= 0;
    }
    return reject(a, b, "<=");
  }

  public static Object more(Object a, Object b) {
    if ((a instanceof Comparable) && (b instanceof Comparable)) {
      return ((Comparable) a).compareTo(b) > 0;
    }
    return reject(a, b, ">");
  }

  public static Object moreorequals(Object a, Object b) {
    if ((a instanceof Comparable) && (b instanceof Comparable)) {
      return ((Comparable) a).compareTo(b) >= 0;
    }
    return reject(a, b, ">=");
  }

  public static Object and(Object a, Object b) {
    if ((a instanceof Boolean) && (b instanceof Boolean)) {
      return ((Boolean) a) && ((Boolean) b);
    }
    return reject(a, b, "and");
  }

  public static Object or(Object a, Object b) {
    if ((a instanceof Boolean) && (b instanceof Boolean)) {
      return ((Boolean) a) || ((Boolean) b);
    }
    return reject(a, b, "or");
  }

  public static Object not(Object a) {
    if (a instanceof Boolean) {
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
