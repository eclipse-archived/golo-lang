package gololang;

public class Predefined {

  public static void print(Object obj) {
    System.out.print(obj);
  }

  public static void println(Object obj) {
    System.out.println(obj);
  }

  public static void require_not_null(Object obj) throws AssertionError {
    if (obj != null) {
      return;
    }
    throw new AssertionError("null reference encountered");
  }

  public static void require(Object condition, Object errorMessage) throws IllegalArgumentException, AssertionError {
    require_not_null(condition);
    require_not_null(errorMessage);
    if ((condition instanceof Boolean) && (errorMessage instanceof String)) {
      if ((Boolean) condition) {
        return;
      }
      throw new AssertionError(errorMessage);
    } else {
      throw new IllegalArgumentException(
          new StringBuilder()
              .append("Wrong parameters for require: expected (Boolean, String) but got (")
              .append(condition.getClass().getName())
              .append(", ")
              .append(errorMessage.getClass().getName())
              .append(")")
              .toString());
    }
  }

  public static Object array(Object... values) {
    return values;
  }

  public static Object a_get(Object a, Object i) {
    require(a instanceof Object[], "a_get takes an array as first parameter");
    require(i instanceof Integer, "a_get takes an index as second parameter");
    Object[] array = (Object[]) a;
    return array[(Integer) i];
  }

  public static void a_set(Object a, Object i, Object value) {
    require(a instanceof Object[], "a_set takes an array as first parameter");
    require(i instanceof Integer, "a_set takes an index as second parameter");
    Object[] array = (Object[]) a;
    array[(Integer) i] = value;
  }

  public static Object a_length(Object a) {
    require(a instanceof Object[], "a_length takes an array as parameter");
    Object[] array = (Object[]) a;
    return array.length;
  }
}
