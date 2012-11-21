package dispatch;

public class Monomorphic {

  public static Object run_boxed() {
    Object i = Integer.valueOf(0);
    Object result = null;
    while ((Integer) i < Integer.valueOf(5000000)) {
      result = i.toString();
      i = (Integer) i + 1;
    }
    return result;
  }

  public static String run_unboxed() {
    int i = 0;
    String result = "";
    while (i < 5000000) {
      result = Integer.valueOf(i).toString(); // Note: String.valueOf(int) would do an invokestatic call site
      i = i + 1;
    }
    return result;
  }
}
