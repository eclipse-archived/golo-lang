package gololang;

import java.util.Arrays;
import java.util.LinkedList;

public class Predefined {

  // ...................................................................................................................

  public static void print(Object obj) {
    System.out.print(obj);
  }

  public static void println(Object obj) {
    System.out.println(obj);
  }

  // ...................................................................................................................

  public static void requireNotNull(Object obj) throws AssertionError {
    if (obj != null) {
      return;
    }
    throw new AssertionError("null reference encountered");
  }

  public static void require(Object condition, Object errorMessage) throws IllegalArgumentException, AssertionError {
    requireNotNull(condition);
    requireNotNull(errorMessage);
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

  // ...................................................................................................................

  public static Object Array(Object... values) {
    return values;
  }

  public static Object asList(Object[] values) {
    return Arrays.asList(values);
  }

  public static Object aget(Object a, Object i) {
    require(a instanceof Object[], "aget takes an Array as first parameter");
    require(i instanceof Integer, "aget takes an index as second parameter");
    Object[] array = (Object[]) a;
    return array[(Integer) i];
  }

  public static void aset(Object a, Object i, Object value) {
    require(a instanceof Object[], "aset takes an Array as first parameter");
    require(i instanceof Integer, "aset takes an index as second parameter");
    Object[] array = (Object[]) a;
    array[(Integer) i] = value;
  }

  public static Object alength(Object a) {
    require(a instanceof Object[], "alength takes an Array as parameter");
    Object[] array = (Object[]) a;
    return array.length;
  }

  // ...................................................................................................................
}
