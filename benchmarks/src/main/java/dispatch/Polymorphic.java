package dispatch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

public class Polymorphic {

  public static Object run() {
    Object[] data = new Object[]{
        "foo",
        666,
        new Object(),
        "bar",
        999,
        new LinkedList<>(),
        new HashMap<>(),
        new TreeSet<>(),
        new RuntimeException(),
        new IllegalArgumentException(),
        new IllegalStateException(),
        new Object(),
        new Exception()
    };
    Object result = null;
    for (int i = 0; i < 1000000; i++) {
      for (int j = 0; j < data.length; j++) {
        result = data[j].toString();
      }
    }
    return result;
  }
}
