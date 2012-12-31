package closure;

public class CallClosure {

  public static Object run_boxed() {
    Object i = Integer.valueOf(0);
    Object result = null;
    while ((Integer) i < Integer.valueOf(2000000)) {
      result = closure(i);
      i = (Integer) i + 1;
    }
    return ">>> " + result;
  }

  public static Object run_unboxed() {
    Object result = null;
    for (int i = 0; i < 2000000; i++) {
      result = closure(i);
    }
    return result;
  }

  private static Object closure(Object value) {
    return "[" + value + "]";
  }
}
