package dispatch;

public class Monomorphic {

  public static Object run() {
    Object i = Integer.valueOf(0);
    Object result = null;
    while ((Integer) i < Integer.valueOf(5000000)) {
      result = i.toString();
      i = (Integer) i + 1;
    }
    return result;
  }
}
